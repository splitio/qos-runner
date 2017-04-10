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
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;
import io.split.qos.server.util.TestId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gives that last time all the tests ran and were green.
 *
 * Check QOSServerState for a definition of what 'green' means.
 */
@Singleton
public class SlackGreenCommand extends SlackAbstractCommand {
    private final QOSServerState state;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackGreenCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender messageSender,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, messageSender, slackCommandGetter);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        Long lastGreen = state.lastGreen();
        if (lastGreen == null) {
            Map<TestId, QOSServerState.TestStatus> failed = state.failedTests();
            if (failed.isEmpty()) {
                messageSender()
                        .sendWarning(slackCommand.command() + " - WAITING CYCLE",
                                "No test failing, waiting cycle of all successful tests.",
                                messagePosted.getChannel(),
                                session);
            } else {
                List<String> failedNames = failed
                        .keySet()
                        .stream()
                        .map(TestId::toString)
                        .collect(Collectors.toList());
                if (failedNames.size() >= 5) {
                    messageSender()
                            .sendFailed(slackCommand.command() + " - NOT GREEN",
                                    String.format("%s tests failing.", failedNames.size()),
                                    messagePosted.getChannel(),
                                    session);
                } else {
                    messageSender()
                            .sendFailed(slackCommand.command() + " - NOT GREEN",
                                    String.format("%s tests failing: \n %s.", failedNames.size(), String.join("\n", failedNames)),
                                    messagePosted.getChannel(),
                                    session);
                }
            }
            return false;
        } else {
            messageSender()
                    .sendSuccess(slackCommand.command(),
                            String.format("Last Green %s %s", dateFormatter.formatDate(lastGreen), state.isPaused() ? "(Paused)" : ""),
                            messagePosted.getChannel(),
                            session);
            return true;
        }
    }

    @Override
    public String description() {
        return "Displays the last time when all the tests passed";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] green";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
