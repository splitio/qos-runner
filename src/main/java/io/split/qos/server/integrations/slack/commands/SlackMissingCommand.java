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
 * Lists all the missing tests.
 */
public class SlackMissingCommand implements SlackCommandExecutor {
    private static final int CHUNK_SIZE = 50;

    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final SlackColors colors;
    private final SlackCommandGetter commandGetter;
    private final SlackAttachmentPartitioner partitioner;

    @Inject
    public SlackMissingCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackCommandGetter slackCommandGetter,
            SlackAttachmentPartitioner slackAttachmentPartitioner,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
        this.commandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.partitioner = Preconditions.checkNotNull(slackAttachmentPartitioner);
        this.colors = slackColors;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {

        List<QOSServerState.TestDTO> missing = state.missingTests();
        if (missing.isEmpty()) {
            String title = String.format("[%s] MISSING TESTS", serverName.toUpperCase());
            String text = "No missing tests";
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
        List<String> missingTests = missing
                .stream()
                .map(value -> String.format("%s", value.name()))
                .collect(Collectors.toList());

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        missingTests
                .stream()
                .forEach(test -> {
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    testAttachment
                            .setColor(colors.getWarning());
                    toBeAdded.add(testAttachment);
                });
        SlackCommand slackCommand = commandGetter.get(messagePosted).get();
        partitioner.send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] missing [server-name]: Displays a lists of the tests that have not run yet";
    }
}

