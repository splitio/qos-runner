package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.split.qos.server.QOSServerConfiguration;
import io.split.qos.server.integrations.slack.AbstractSlackIntegration;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSServerModule;

import java.io.IOException;

public class SlackCommandIntegrationImpl extends AbstractSlackIntegration implements SlackCommandIntegration {
    private final SlackCommandListener slackCommandListener;
    private final String serverName;
    private final SlackCommandRegisterer register;
    private final SlackCommandGetter slackCommandGetter;

    @Inject
    public SlackCommandIntegrationImpl(
            QOSServerConfiguration configuration,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandListener slackCommandListener,
            SlackCommandRegisterer registerer,
            SlackCommon slackCommon,
            SlackCommandGetter slackCommandGetter) {
        super(configuration.getSlack().getBotToken(),
                configuration.getSlack().getDigestChannel(),
                configuration.getSlack().getVerboseChannel(),
                slackCommon);
        this.slackCommandListener = Preconditions.checkNotNull(slackCommandListener);
        this.serverName = Preconditions.checkNotNull(serverName);
        this.register = Preconditions.checkNotNull(registerer);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
    }

    public void initialize() throws IOException {
        initialize(true);
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
        slackSession()
                .addMessagePostedListener(slackMessagePostedListener);
    }

    @Override
    public void close() throws Exception {
        close(true);
    }

    private boolean isBot(SlackMessagePosted message) {
        String content = message.getMessageContent();
        return content.trim().startsWith(String.format("<@%s>", botId()));
    }

}
