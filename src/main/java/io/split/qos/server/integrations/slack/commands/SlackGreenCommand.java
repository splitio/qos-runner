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
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Gives that last time all the tests ran and were green.
 *
 * Check QOSServerState for a definition of what 'green' means.
 */
@Singleton
public class SlackGreenCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final SlackColors colors;

    @Inject
    public SlackGreenCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = slackColors;
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Long lastGreen = state.lastGreen();
        if (lastGreen == null) {
            String title = String.format("[%s] NOT GREEN", serverName.toUpperCase());
            SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
            List<QOSServerState.TestDTO> failed = state.failedTests();
            if (failed.isEmpty()) {
                slackAttachment.setTitle(String.format("[%s] WAITING CYCLE", serverName.toUpperCase()));
                slackAttachment.setText("No test failing, waiting cycle of all successful tests.");
                slackAttachment.setColor(colors.getWarning());
            } else {
                slackAttachment.setColor(colors.getFailed());
                List<String> failedNames = failed
                        .stream()
                        .map(QOSServerState.TestDTO::name)
                        .collect(Collectors.toList());
                if (failedNames.size() >= 5) {
                    slackAttachment.setText(String.format("%s tests failing.", failedNames.size()));
                } else {
                    slackAttachment.setText(String.format("%s tests failing: \n %s.", failedNames.size(), String.join("\n", failedNames)));
                }
            }
            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);
        } else {
            String title = String.format("[%s] GREEN", serverName.toUpperCase());
            SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
            slackAttachment.setText(String.format("Last Green %s %s", dateFormatter.formatDate(lastGreen), state.isPaused() ? "(Paused)" : ""));
            slackAttachment.setColor(colors.getSuccess());
            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);
        }
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] green: Displays the last time when all the tests passed";
    }
}
