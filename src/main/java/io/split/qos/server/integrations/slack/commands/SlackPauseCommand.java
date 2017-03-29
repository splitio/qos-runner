package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.SlackColors;

import java.util.List;

/**
 * Pauses the server. No new tests will be added, currently running tests
 * will finish.
 */
@Singleton
public class SlackPauseCommand extends SlackAbstractCommand {
    private final QOSServerBehaviour behaviour;

    @Inject
    public SlackPauseCommand(
            QOSServerBehaviour behaviour,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.behaviour = behaviour;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        behaviour.pause(messagePosted.getSender().getUserName());

        messageSender()
                .sendWarning(slackCommand.command(),
                        "Server PAUSED by " + messagePosted.getSender().getUserName(),
                        messagePosted.getChannel(),
                        session);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] pause: Pauses the tests running for the server. Current tests will keep running";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
