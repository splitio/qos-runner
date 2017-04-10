package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;

/**
 * Slacks some info about the server.
 */
@Singleton
public class SlackInfoCommand extends SlackAbstractCommand {

    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final String suites;
    private final String description;

    @Inject
    public SlackInfoCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            @Named(QOSPropertiesModule.SUITES) String suites,
            @Named(QOSPropertiesModule.DESCRIPTION) String description) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.state = state;
        this.description = Preconditions.checkNotNull(description);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.suites = Preconditions.checkNotNull(suites);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        messageSender()
                .send(slackCommand.command(), session, messagePosted.getChannel(), null);

        if (state.isActive()) {
            String text = String.format("Description: %s \n\nStatus: %s [since %s]\nLast test finished: %s\nSuites: %s\nResumed by: %s",
                    description,
                    state.status(),
                    dateFormatter.formatDate(state.activeSince()),
                    dateFormatter.formatDate(state.lastTestFinished()),
                    suites,
                    state.who());
            messageSender()
                    .sendInfo(slackCommand.command(),
                            text,
                            messagePosted.getChannel(),
                            session);
        }
        if (state.isPaused()) {
            String text = String.format("Description: %s \n\nStatus: %s [since %s]\nLast test finished: %s\nSuites: %s\nPaused by: %s",
                    description,
                    state.status(),
                    dateFormatter.formatDate(state.pausedSince()),
                    dateFormatter.formatDate(state.lastTestFinished()),
                    suites,
                    state.who());
            messageSender()
                    .sendWarning(slackCommand.command(),
                            text,
                            messagePosted.getChannel(),
                            session);
        }

        return true;
    }

    @Override
    public String description() {
        return "Displays Server Info, like status, time since status change, last test ran";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] info";
    }


    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
