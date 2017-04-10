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
 * Resumes the server, new tests are added to be run.
 */
@Singleton
public class SlackResumeCommand extends SlackAbstractCommand {
    private final QOSServerBehaviour behaviour;

    @Inject
    public SlackResumeCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.behaviour = behaviour;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        behaviour.resume(messagePosted.getSender().getUserName());
        messageSender()
                .sendSuccess(slackCommand.command(),
                        "Server RESUMED by " + messagePosted.getSender().getUserName(),
                        messagePosted.getChannel(),
                        session);
        return true;
    }

    @Override
    public String description() {
        return "Resumes the tests of the server";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] resume";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
