package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

/**
 * Slacks some info about the server.
 */
@Singleton
public class SlackInfoCommand implements SlackCommandExecutor {

    private final QOSServerState state;
    private final String serverName;
    private final DateFormatter dateFormatter;
    private final String suites;
    private final SlackColors colors;
    private final String description;

    @Inject
    public SlackInfoCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            @Named(QOSPropertiesModule.SUITES) String suites,
            @Named(QOSPropertiesModule.DESCRIPTION) String description) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.state = state;
        this.colors = slackColors;
        this.description = Preconditions.checkNotNull(description);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.suites = Preconditions.checkNotNull(suites);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String text = null;
        if (state.isActive()) {
            text = String.format("Description: %s \n\nStatus: %s [since %s]\nLast test finished: %s\nSuites: %s\nResumed by: %s",
                    description,
                    state.status(),
                    dateFormatter.formatDate(state.activeSince()),
                    dateFormatter.formatDate(state.lastTestFinished()),
                    suites,
                    state.who());
        }
        if (state.isPaused()) {
            text = String.format("Description: %s \n\nStatus: %s [since %s]\nLast test finished: %s\nSuites: %s\nPaused by: %s",
                    description,
                    state.status(),
                    dateFormatter.formatDate(state.pausedSince()),
                    dateFormatter.formatDate(state.lastTestFinished()),
                    suites,
                    state.who());
        }
        String title = String.format("[%s] INFO", serverName.toUpperCase());
        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, "");
        slackAttachment

                .setColor(state.isActive() ? colors.getInfo() : colors.getWarning());
        SlackPreparedMessage sent = new SlackPreparedMessage
                .Builder()
                .addAttachment(slackAttachment)
                .build();
        session
                .sendMessage(messagePosted.getChannel(), sent);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] info: Displays Server Info, like status, time since status change, last test ran";
    }
}
