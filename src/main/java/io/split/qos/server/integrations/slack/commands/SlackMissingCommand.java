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
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;
import io.split.qos.server.util.TestId;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lists all the missing tests.
 */
public class SlackMissingCommand extends SlackAbstractCommand {

    private final QOSServerState state;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackMissingCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        Map<TestId, QOSServerState.TestStatus> missing = state.missingTests();
        if (missing.isEmpty()) {
            messageSender()
                    .sendSuccess(slackCommand.command(),
                            "No missing tests",
                            messagePosted.getChannel(),
                            session);
            return true;
        }
        List<String> missingTests = missing
                .keySet()
                .stream()
                .map(value -> value.toString())
                .collect(Collectors.toList());

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        missingTests
                .stream()
                .forEach(test -> {
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    testAttachment
                            .setColor(colors().getWarning());
                    toBeAdded.add(testAttachment);
                });
        messageSender().sendPartition(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] missing [server-name]: Displays a lists of the tests that have not run yet";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}

