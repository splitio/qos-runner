package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
    private static final int CHUNK_SIZE = 50;

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

        List<QOSServerState.TestDTO> missing = state.missingTests();
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
        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, CHUNK_SIZE);

        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            String title = String.format("[%s] MISSING TESTS", serverName.toUpperCase());
            String text = String.format("Total Missing Tests %s / %s, tests %s - %s",
                    missing.size(),
                    state.tests().size(),
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
        return "missing [server-name]: Displays a lists of the tests that have not run yet";
    }
}

