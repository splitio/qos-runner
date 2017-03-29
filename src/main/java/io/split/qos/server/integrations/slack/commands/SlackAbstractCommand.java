package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.SlackColors;

public abstract class SlackAbstractCommand implements SlackCommandExecutor {

    private final SlackColors colors;
    private final String serverName;
    private final SlackMessageSender messageSender;
    private final SlackCommandGetter commandGetter;

    public SlackAbstractCommand(SlackColors slackColors,
                                String serverName,
                                SlackMessageSender slackMessageSender,
                                SlackCommandGetter slackCommandGetter) {
        this.colors = Preconditions.checkNotNull(slackColors);
        this.serverName = Preconditions.checkNotNull(serverName);
        this.messageSender = Preconditions.checkNotNull(slackMessageSender);
        this.commandGetter = Preconditions.checkNotNull(slackCommandGetter);
    }

    public SlackCommand command(SlackMessagePosted messagePosted) {
        return commandGetter().get(messagePosted).get();
    }

    public SlackColors colors() {
        return colors;
    }

    public String serverName() {
        return serverName;
    }

    public SlackMessageSender messageSender() {
        return messageSender;
    }

    public SlackCommandGetter commandGetter() {
        return commandGetter;
    }
}
