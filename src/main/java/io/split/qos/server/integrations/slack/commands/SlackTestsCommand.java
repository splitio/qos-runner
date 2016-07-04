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

import java.util.Map;

/**
 * Runs one test of the server
 */
@Singleton
public class SlackTestsCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackTestsCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Map<String, QOSServerState.TestStatus> tests = state.tests();
        String title = String.format("[%s] Tests QOS Server", serverName);
        String text = String.format("Total Tests %s", tests.size());

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("good");

        StringBuilder testsList = new StringBuilder();
        testsList.append("```\n");
        testsList.append(" class#test name | time last run | status last run\n");
        tests.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    if (o1.getValue().succeeded() == null && o2.getValue().succeeded() == null) {
                        return 0;
                    }
                    if (o1.getValue().succeeded() == null) {
                        return 1;
                    }
                    if (o2.getValue().succeeded() == null) {
                        return -1;
                    }
                    return o1.getValue().succeeded().compareTo(o2.getValue().succeeded());
                })
                .forEach(value -> {
                    String status = null;
                    if (value.getValue().succeeded() == null) {
                        status = "--";
                    } else if (value.getValue().succeeded()) {
                        status = "PASSED";
                    } else {
                        status = "FAILED";
                    }
                    testsList
                            .append(String.format("%s | %s | %s\n",
                                    value.getKey(),
                                    dateFormatter.formatDate(value.getValue().when()),
                                    status));
                });
        testsList.append("```\n");
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        session
                .sendMessage(messagePosted.getChannel(),
                        testsList.toString());
        return true;
    }

    @Override
    public String help() {
        return "tests [server-name]: Displays a lists of the tests that the server runs";
    }
}
