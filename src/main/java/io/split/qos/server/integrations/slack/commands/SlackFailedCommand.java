package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lists all the failed tests, ordered by time.
 */
public class SlackFailedCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final SlackColors colors;

    @Inject
    public SlackFailedCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
        this.colors = slackColors;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Map<String, QOSServerState.TestStatus> tests = state.tests();
        String title = String.format("[%s] Failed Tests", serverName.toUpperCase());

        List<Map.Entry<String, QOSServerState.TestStatus>> failed = tests.entrySet()
                .stream()
                .filter(entry -> (entry.getValue().succeeded() != null && !entry.getValue().succeeded()))
                .sorted((o1, o2) -> o1.getValue().when().compareTo(o2.getValue().when()))
                .collect(Collectors.toList());
        List<String> failedTests = failed
                .stream()
                .map(value -> String.format("%s | %s",
                        value.getKey(),
                        dateFormatter.formatDate(value.getValue().when())))
                .collect(Collectors.toList());

        String text = String.format("Total Failed Tests %s / %s", failed.size(), tests.size());
        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor(failed.isEmpty() ? colors.getSuccess() : colors.getFailed());

        SlackPreparedMessage.Builder sent = new SlackPreparedMessage
                .Builder()
                .addAttachment(slackAttachment);

        if (!failed.isEmpty()) {
            failedTests
                    .stream()
                    .forEach(failedTest -> {
                        SlackAttachment failedAttachment = new SlackAttachment("", "", failedTest, null);
                        failedAttachment
                                .setColor(colors.getWarning());
                        sent.addAttachment(failedAttachment);
                    });
        }
        session
                .sendMessage(messagePosted.getChannel(), sent.build());

        return true;
    }

    @Override
    public String help() {
        return "failed [server-name]: Displays a lists of the failed tests";
    }
}

