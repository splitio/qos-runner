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
import java.util.stream.Collectors;

/**
 * Lists all the missing tests.
 */
public class SlackMissingCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final SlackColors colors;

    @Inject
    public SlackMissingCommand(
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
        String title = String.format("[%s] MISSING TESTS", serverName.toUpperCase());

        List<QOSServerState.TestDTO> missing = state.missingTests();
        List<String> missingTests = missing
                .stream()
                .map(value -> String.format("%s", value.name()))
                .collect(Collectors.toList());

        String text = String.format("Total Missing Tests %s / %s", missing.size(), state.tests().size());
        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor(missing.isEmpty() ? colors.getSuccess() : colors.getFailed());

        SlackPreparedMessage.Builder sent = new SlackPreparedMessage
                .Builder()
                .addAttachment(slackAttachment);

        if (!missingTests.isEmpty()) {
            missingTests
                    .stream()
                    .forEach(missingTest -> {
                        SlackAttachment missingAttachment = new SlackAttachment("", "", missingTest, null);
                        missingAttachment
                                .setColor(colors.getWarning());
                        sent.addAttachment(missingAttachment);
                    });
        }
        session
                .sendMessage(messagePosted.getChannel(), sent.build());

        return true;
    }

    @Override
    public String help() {
        return "missing [server-name]: Displays a lists of the tests that have not run yet";
    }
}

