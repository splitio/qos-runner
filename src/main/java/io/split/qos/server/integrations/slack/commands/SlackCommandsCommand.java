package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all the available commands that the bot can process.
 */
public class SlackCommandsCommand extends SlackAbstractCommand {


    private final SlackCommandListener listener;

    @Inject
    public SlackCommandsCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender messageSender,
            SlackCommandListener listener,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, messageSender, slackCommandGetter);
        this.listener = Preconditions.checkNotNull(listener);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand command = command(messagePosted);
        List<SlackAttachment> slacks = listener
                .commands()
                .stream()
                .map(help -> {
                    SlackAttachment slackAttachment = new SlackAttachment(help.arguments(), "", help.description(), null);
                    slackAttachment.setColor(colors().getInfo());
                    return slackAttachment;
                })
                .collect(Collectors.toList());
        messageSender()
                .send(command.command(), session, messagePosted.getChannel(), slacks);
        return true;
    }

    @Override
    public String description() {
        return "Displays the list of commands the server accepts";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] commands";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
