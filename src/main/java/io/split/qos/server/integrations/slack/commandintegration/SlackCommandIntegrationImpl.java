package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.split.qos.server.integrations.slack.SlackSessionProvider;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackCommandIntegrationImpl implements SlackCommandIntegration {
    private static final Logger LOG = LoggerFactory.getLogger(SlackCommandIntegrationImpl.class);

    private final SlackCommandListener slackCommandListener;
    private final String serverName;
    private final SlackCommandRegisterer register;
    private final SlackCommandGetter slackCommandGetter;
    private final SlackSessionProvider slackSessionProvider;

    @Inject
    public SlackCommandIntegrationImpl(
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandListener slackCommandListener,
            SlackCommandRegisterer registerer,
            SlackSessionProvider slackSessionProvider,
            SlackCommandGetter slackCommandGetter) {
        this.slackCommandListener = Preconditions.checkNotNull(slackCommandListener);
        this.serverName = Preconditions.checkNotNull(serverName);
        this.register = Preconditions.checkNotNull(registerer);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.slackSessionProvider = Preconditions.checkNotNull(slackSessionProvider);
    }

    /**
     * Starts listening on Slack and executes commands whenever qosbot is referenced.
     */
    public void startBotListener() {
        register.register();
        SlackMessagePostedListener slackMessagePostedListener = (event, session) -> {
            if (isBot(event)) {
                slackCommandGetter.get(event)
                        .ifPresent(command -> {
                            if (!command.server().isPresent() || command.server().get().equalsIgnoreCase(serverName)) {
                                slackCommandListener.execute(command, event, session);
                            }
                        });
            }
        };
        slackSessionProvider
                .slackSession()
                .addMessagePostedListener(slackMessagePostedListener);
    }

    private boolean isBot(SlackMessagePosted message) {
        String content = message.getMessageContent();
        return content.trim().startsWith(String.format("<@%s>", slackSessionProvider.botId()));
    }

    @Override
    public boolean isEnabled() {
        return slackSessionProvider.isEnabled();
    }
}
