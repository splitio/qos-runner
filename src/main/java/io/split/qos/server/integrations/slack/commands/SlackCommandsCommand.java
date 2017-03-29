package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all the available commands that the bot can process.
 */
public class SlackCommandsCommand implements SlackCommandExecutor {

    private final String serverName;
    private final SlackCommandListener listener;
    private final SlackColors colors;

    @Inject
    public SlackCommandsCommand(
            SlackCommandListener listener,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = slackColors;
        this.listener = Preconditions.checkNotNull(listener);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format("[%s] COMMANDS", serverName.toUpperCase());

        List<String> commands = listener
                .commands()
                .stream()
                .map(SlackCommandExecutor::help)
                .collect(Collectors.toList());


        SlackAttachment slackAttachment = new SlackAttachment(title, "", String.join("\n", commands), null);
        slackAttachment
                .setColor(colors.getInfo());
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        return true;
    }

    @Override
    public String help() {
        return "commands [server-name]: Displays the list of commands the server accepts";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
