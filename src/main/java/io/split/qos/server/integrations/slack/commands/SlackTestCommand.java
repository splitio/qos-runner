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
import io.split.qos.server.QOSTestsTracker;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;
import io.split.testrunner.util.Util;

import java.util.List;
import java.util.Optional;

/**
 * Shows the tests that matches the input.
 */
@Singleton
public class SlackTestCommand implements SlackCommandExecutor {
    private static final String TITLE = "[%s] TESTS";

    private final String serverName;
    private final DateFormatter dateFormatter;
    private final QOSTestsTracker tracker;
    private static final int CHUNK_SIZE = 50;
    private final SlackColors colors;
    private final SlackCommandGetter slackCommandGetter;
    private final QOSServerState state;

    @Inject
    public SlackTestCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            DateFormatter dateFormatter,
            QOSTestsTracker  tracker,
            QOSServerState state,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.colors = Preconditions.checkNotNull(slackColors);
        this.tracker = Preconditions.checkNotNull(tracker);
        this.state = Preconditions.checkNotNull(state);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = slackCommandGetter.get(messagePosted).get();

        List<String> arguments = slackCommand.arguments();
        if (arguments == null || arguments.isEmpty()) {
            slackError(messagePosted, session);
            return false;
        }
        List<QOSTestsTracker.Tracked> tests = null;
        if (arguments.size() == 1) {
            tests = tracker.getTests(Optional.empty(), arguments.get(0));
        } else {
            tests = tracker.getTests(Optional.of(arguments.get(0)), arguments.get(1));
        }
        if (tests.isEmpty()) {
            slackEmpty(messagePosted, arguments, session);
            return false;
        }

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        tests
                .stream()
                .map(tracked -> tracked.method())
                .forEach(value -> {
                    String id = Util.id(value);
                    SlackAttachment testAttachment = new SlackAttachment("", "", id, null);
                    QOSServerState.TestStatus status = state.test(value);
                    if (status.succeeded() == null) {
                        testAttachment.setColor(colors.getWarning());
                    } else if (status.succeeded()) {
                        testAttachment.setColor(colors.getSuccess());
                    } else {
                        testAttachment.setColor(colors.getFailed());
                    }
                    toBeAdded.add(testAttachment);
                });
        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, CHUNK_SIZE);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            String title = String.format("[%s] TEST", serverName.toUpperCase());
            String text = String.format("Total Tests %s, tests %s - %s",
                    tests.size(),
                    1 + CHUNK_SIZE * iteration,
                    CHUNK_SIZE * iteration + partitions.get(index).size());

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment.setColor(colors.getInfo());

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

    private void slackEmpty(SlackMessagePosted messagePosted, List<String> arguments, SlackSession session) {
        String title = String.format(TITLE, serverName.toUpperCase());
        String text = "No test matched " + arguments.toString();
        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("warning");
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
    }

    private void slackError(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format(TITLE, serverName.toUpperCase());
        String text = "Tests needs an actual test to search";
        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("warning");
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
    }

    @Override
    public String help() {
        return "test [server-name] [class name (optional)] [test name]: Finds the tests that matches the parameters";
    }
}
