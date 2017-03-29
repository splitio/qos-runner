package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
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
import java.util.Map;

/**
 * Shows all the tests of the QOS.
 */
@Singleton
public class SlackTestsCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;

    private static final int CHUNK_SIZE = 50;
    private final SlackColors colors;
    private final SlackCommandGetter commandGetter;
    private final SlackAttachmentPartitioner partitioner;

    @Inject
    public SlackTestsCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackAttachmentPartitioner slackAttachmentPartitioner,
            QOSServerState state,
            DateFormatter dateFormatter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
        this.partitioner = Preconditions.checkNotNull(slackAttachmentPartitioner);
        this.commandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.colors = slackColors;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Map<String, QOSServerState.TestStatus> tests = state.tests();
        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        tests.entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    if (o1.getValue().hasFinished() &&  !o1.getValue().succeeded()) {
                        return 1;
                    }
                    if (o2.getValue().hasFinished() &&  !o2.getValue().succeeded()) {
                        return -1;
                    }
                    if (!o1.getValue().hasFinished() && !o2.getValue().hasFinished()) {
                        return 0;
                    }
                    if (!o1.getValue().hasFinished()) {
                        return -1;
                    }
                    if (!o2.getValue().hasFinished()) {
                        return 1;
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
        SlackCommand slackCommand = commandGetter.get(messagePosted).get();
        partitioner.send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] tests: Displays a lists of the tests that the server runs";
    }
}
