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
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;
import io.split.qos.server.util.TestId;

import java.util.List;
import java.util.Map;

/**
 * Shows the tests that matches the input.
 */
@Singleton
public class SlackTestsCommand extends SlackAbstractCommand {
    private final DateFormatter dateFormatter;
    private final QOSServerState state;

    @Inject
    public SlackTestsCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            DateFormatter dateFormatter,
            SlackMessageSender slackMessageSender,
            QOSServerState state,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = Preconditions.checkNotNull(state);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        List<String> arguments = slackCommand.arguments();

        Map<TestId, QOSServerState.TestStatus> tests = null;
        if (arguments.size() == 0) {
            tests = state.tests();
        } else if (arguments.size() == 1) {
            tests = state.tests(arguments.get(0));
        } else {
            tests = state.tests(arguments.get(0), arguments.get(1));
        }

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        tests
                .entrySet()
                .stream()
                .sorted((o1, o2) -> {
                    QOSServerState.TestStatus statusO1 = o1.getValue();
                    QOSServerState.TestStatus statusO2 = o2.getValue();
                    if (statusO1.hasFinished() &&  !statusO1.succeeded()) {
                        return 1;
                    }
                    if (statusO2.hasFinished() &&  !statusO2.succeeded()) {
                        return -1;
                    }
                    if (!statusO1.hasFinished() && !statusO2.hasFinished()) {
                        return 0;
                    }
                    if (!statusO1.hasFinished()) {
                        return -1;
                    }
                    if (!statusO2.hasFinished()) {
                        return 1;
                    }
                    return statusO1.succeeded().compareTo(statusO2.succeeded());
                })
                .forEach(value -> {
                    TestId id = value.getKey();
                    SlackAttachment testAttachment = new SlackAttachment("", "", id.toString(), null);
                    QOSServerState.TestStatus status = value.getValue();
                    if (status.succeeded() == null) {
                        testAttachment.setColor(colors().getWarning());
                    } else if (status.succeeded()) {
                        testAttachment.setColor(colors().getSuccess());
                    } else {
                        testAttachment.setColor(colors().getFailed());
                    }
                    toBeAdded.add(testAttachment);
                });
        messageSender().send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] tests [class name (optional)] [test name (optional)]: " +
                "Finds the tests that matches the parameters. No parameters gets all tests.";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() < 3;
    }
}
