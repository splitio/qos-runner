package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.modules.QOSServerModule;

public class SlackCommandsCommand implements SlackCommandExecutor {

    private final String serverName;
    private final SlackCommandListener listener;

    @Inject
    public SlackCommandsCommand(
            SlackCommandListener listener,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.listener = listener;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format("Commands QOS Server '%s'", serverName);

        SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
        slackAttachment
                .setColor("good");

        StringBuilder commandsList = new StringBuilder();
        commandsList.append("```");
        listener.commands()
                .stream()
                .forEach(command -> commandsList
                        .append(command.help())
                        .append("\n"));
        commandsList.append("```");

        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        session
                .sendMessage(messagePosted.getChannel(),
                        commandsList.toString());
        return true;
    }

    @Override
    public String help() {
        return "commands [server-name]: Displays the list of commands the server accepts";
    }
}
