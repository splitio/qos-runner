package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.split.qos.server.integrations.slack.AbstractSlackIntegration;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;

public class SlackCommandIntegrationImpl extends AbstractSlackIntegration implements SlackCommandIntegration {
    private final boolean enabled;
    private final SlackCommandListener slackCommandListener;
    private final String serverName;
    private final SlackCommandRegisterer register;
    private final SlackCommandGetter slackCommandGetter;

    @Inject
    public SlackCommandIntegrationImpl(
            @Named(QOSPropertiesModule.SLACK_INTEGRATION) String slackIntegration,
            @Named(QOSPropertiesModule.SLACK_BOT_TOKEN) String slackBotToken,
            @Named(QOSPropertiesModule.SLACK_DIGEST_CHANNEL) String slackDigestChannel,
            @Named(QOSPropertiesModule.SLACK_VERBOSE_CHANNEL) String slackVerboseChannel,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandListener slackCommandListener,
            SlackCommandRegisterer registerer,
            SlackCommon slackCommon,
            SlackCommandGetter slackCommandGetter) {
        super(slackBotToken, slackDigestChannel, slackVerboseChannel, slackCommon);
        this.enabled = Boolean.valueOf(Preconditions.checkNotNull(slackIntegration));
        this.slackCommandListener = Preconditions.checkNotNull(slackCommandListener);
        this.serverName = Preconditions.checkNotNull(serverName);
        this.register = Preconditions.checkNotNull(registerer);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public void initialize() {
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
