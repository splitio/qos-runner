package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSServerModule;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all the available commands that the bot can process.
 */
public class SlackCommandsCommand implements SlackCommandExecutor {

    private final String serverName;
    private final SlackCommandListener listener;

    @Inject
    public SlackCommandsCommand(
            SlackCommandListener listener,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.listener = Preconditions.checkNotNull(listener);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format("[%s] Commands", serverName.toUpperCase());

        List<String> commands = listener
                .commands()
                .stream()
                .map(SlackCommandExecutor::help)
                .collect(Collectors.toList());


        SlackAttachment slackAttachment = new SlackAttachment(title, "", String.join("\n", commands), null);
        slackAttachment
                .setColor("good");
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
}
