package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.qos.server.util.TestId;
import io.split.testrunner.util.SlackColors;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Runs all the tests of the QOS.
 */
public class SlackRunAllCommand extends SlackAbstractCommand {
    private final QOSServerBehaviour behaviour;
    private final QOSServerState state;
    private static final String SHOW_ARGUMENT = "show";

    @Inject
    public SlackRunAllCommand(
            SlackColors slackColors,
            QOSServerBehaviour behaviour,
            QOSServerState state,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.state = Preconditions.checkNotNull(state);
        this.behaviour = Preconditions.checkNotNull(behaviour);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        if (state.isPaused()) {
            behaviour.resume(messagePosted.getSender().getUserName());
        }
        List<Method> tests = behaviour.runAllNow();
        if (slackCommand.arguments().isEmpty()) {
            messageSender()
                    .send(slackCommand.command(), "Total tests to run: " + tests.size(), session, messagePosted.getChannel());
        } else {
            List<SlackAttachment> toBeAdded = Lists.newArrayList();
            tests
                    .stream()
                    .map(method -> TestId.fromMethod(method))
                    .sorted(TestId::compareTo)
                    .forEach(test -> {
                        SlackAttachment testAttachment = new SlackAttachment("", "", test.toString(), null);
                        testAttachment
                                .setColor(colors().getWarning());
                        toBeAdded.add(testAttachment);
                    });
            messageSender().sendPartition(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        }
        return true;
    }

    @Override
    public String description() {
        return "Runs all the tests of the qos server immediately. If show is present, show the tests that are going to be run.";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] runall [show (optional)]";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0 || (arguments.size() == 1 && arguments.get(0).equalsIgnoreCase(SHOW_ARGUMENT));
    }
}
