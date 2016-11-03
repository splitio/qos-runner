package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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

/**
 * Runs one test of the server
 */
@Singleton
public class SlackTestsCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;

    private static final int CHUNK_SIZE = 50;
    private final SlackColors colors;

    @Inject
    public SlackTestsCommand(
            SlackColors slackColors,
            QOSServerState state,
            DateFormatter dateFormatter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
        this.colors = slackColors;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Map<String, QOSServerState.TestStatus> tests = state.tests();
        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        tests.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    if (!o1.getValue().hasFinished() && !o2.getValue().hasFinished()) {
                        return 0;
                    }
                    if (!o1.getValue().hasFinished()) {
                        return 1;
                    }
                    if (!o2.getValue().hasFinished()) {
                        return -1;
                    }
                    return o1.getValue().succeeded().compareTo(o2.getValue().succeeded());
                })
                .forEach(value -> {
                    String test = String.format("%s | %s",
                            value.getKey(),
                            dateFormatter.formatDate(value.getValue().when()));
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    if (value.getValue().succeeded() == null) {
                        testAttachment.setColor(colors.getWarning());
                    } else if (value.getValue().succeeded()) {
                        testAttachment.setColor(colors.getSuccess());
                    } else {
                        testAttachment.setColor(colors.getFailed());
                    }
                    toBeAdded.add(testAttachment);
                });
        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, CHUNK_SIZE);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            String title = String.format("[%s] TESTS", serverName.toUpperCase());
            String text = String.format("Total Tests %s, tests %s - %s",
                    tests.size(),
                    1 + CHUNK_SIZE * iteration,
                    CHUNK_SIZE * iteration + partitions.get(index).size());

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor(colors.getInfo());

            SlackPreparedMessage.Builder partitionSend = new SlackPreparedMessage
                    .Builder()
                    .addAttachment(slackAttachment)
                    .addAttachments(partitions.get(index));
            session.sendMessage(
                    messagePosted.getChannel(),
                    partitionSend.build());
            iteration++;
        }
        return true;
    }

    @Override
    public String help() {
        return "tests [server-name]: Displays a lists of the tests that the server runs";
    }
}
