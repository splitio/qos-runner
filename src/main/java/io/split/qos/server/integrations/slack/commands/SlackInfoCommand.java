package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.DateFormatter;

/**
 * Slacks some info about the server.
 */
@Singleton
public class SlackInfoCommand implements SlackCommandExecutor {

    private final QOSServerState state;
    private final String serverName;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackInfoCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.state = state;
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String text = null;
        if (state.isActive()) {
            text = String.format("Status %s since %s, Last test finished %s, resumed by %s",
                    state.status(),
                    dateFormatter.formatDate(state.activeSince()),
                    dateFormatter.formatDate(state.lastTestFinished()),
                    state.who());
        }
        if (state.isPaused()) {
            text = String.format("Status %s since %s, Last test finished %s, paused by %s",
                    state.status(),
                    dateFormatter.formatDate(state.pausedSince()),
                    dateFormatter.formatDate(state.lastTestFinished()),
                    state.who());
        }
        String title = String.format("INFO QOS Server '%s'", serverName);
        SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
        slackAttachment
                .setColor(state.isActive() ? "good" : "warning");

        StringBuilder info = new StringBuilder();
        info.append("```");
        info.append(text);
        info.append("```");

        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        session
                .sendMessage(messagePosted.getChannel(),
                        info.toString());
        return true;
    }

    @Override
    public String help() {
        return "info [server-name]: Displays Server Info, like status, time since status change, last test ran";
    }
}
