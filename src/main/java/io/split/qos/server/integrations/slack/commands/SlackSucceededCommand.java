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
import io.split.qos.server.util.TestId;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created on 3/30/17.
 */
public class SlackSucceededCommand extends SlackAbstractCommand {

    private final QOSServerState state;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackSucceededCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            SlackMessageSender slackMessageSender,
            SlackCommandGetter slackCommandGetter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Map<TestId, QOSServerState.TestStatus> succeeded = state.succeededTests();
        SlackCommand slackCommand = command(messagePosted);
        if (succeeded.isEmpty()) {
            messageSender()
                    .sendSuccess(slackCommand.command(), "No succeeded tests", messagePosted.getChannel(), session);
            return true;
        }
        List<String> succeededTests = succeeded
                .entrySet()
                .stream()
                .map(value -> String.format("%s | %s",
                        value.getKey(),
                        dateFormatter.formatDate(value.getValue().when())))
                .collect(Collectors.toList());

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        succeededTests
                .stream()
                .forEach(test -> {
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    testAttachment
                            .setColor(colors().getSuccess());
                    toBeAdded.add(testAttachment);
                });
        messageSender().sendPartition(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] succeeded: Displays a lists of the succeeded tests";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
