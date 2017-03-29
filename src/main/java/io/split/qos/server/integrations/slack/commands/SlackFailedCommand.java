package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackAttachmentPartitioner;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Lists all the failed tests, ordered by time.
 */
public class SlackFailedCommand implements SlackCommandExecutor {
    private static final int CHUNK_SIZE = 50;

    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final SlackColors colors;
    private final SlackAttachmentPartitioner partitioner;
    private final SlackCommandGetter slackCommandGetter;

    @Inject
    public SlackFailedCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            SlackAttachmentPartitioner slackAttachmentPartitioner,
            SlackCommandGetter slackCommandGetter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
        this.colors = slackColors;
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.partitioner = Preconditions.checkNotNull(slackAttachmentPartitioner);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        List<QOSServerState.TestDTO> failed = state.failedTests();
        if (failed.isEmpty()) {
            String title = String.format("[%s] FAILED TESTS", serverName.toUpperCase());
            String text = "No failed tests";
            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor(colors.getSuccess());
            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);
            return true;
        }
        List<String> failedTests = failed
                .stream()
                .map(value -> String.format("%s | %s",
                        value.name(),
                        dateFormatter.formatDate(value.status().when())))
                .collect(Collectors.toList());

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        failedTests
                .stream()
                .forEach(test -> {
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    testAttachment
                            .setColor(colors.getFailed());
                    toBeAdded.add(testAttachment);
                });
        SlackCommand slackCommand = slackCommandGetter.get(messagePosted).get();
        partitioner.send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] failed: Displays a lists of the failed tests";
    }
}

