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
import io.split.testrunner.util.SlackColors;
import io.split.qos.server.util.TestId;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class SlackRunTestCommand extends SlackAbstractCommand {

    private final QOSServerBehaviour behaviour;
    private final QOSServerState state;

    @Inject
    public SlackRunTestCommand(
            SlackColors slackColors,
            SlackMessageSender slackMessageSender,
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandGetter slackCommandGetter,
            QOSServerState state) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.behaviour = behaviour;
        this.state = Preconditions.checkNotNull(state);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        List<String> arguments = slackCommand.arguments();
        List<Method> methodsExecuted;
        if (arguments.size() == 1) {
            methodsExecuted = behaviour.runTestsNow(Optional.empty(), arguments.get(0));
        } else {
            methodsExecuted = behaviour.runTestsNow(Optional.of(arguments.get(0)), arguments.get(1));
        }
        if (methodsExecuted.isEmpty()) {
            messageSender()
                    .sendFailed(slackCommand.command(),
                            String.format("Could not find tests %s", arguments),
                            messagePosted.getChannel(),
                            session);
        } else {
            List<SlackAttachment> toBeAdded = Lists.newArrayList();
            methodsExecuted
                    .stream()
                    .forEach(value -> {
                        TestId id = TestId.fromMethod(value);
                        SlackAttachment testAttachment = new SlackAttachment("", "", id.toString(), null);
                        QOSServerState.TestStatus status = state.test(value);
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
        return false;
    }

    @Override
    public String help() {
        return "[server-name (optional)] run [class name (optional)] [test name]: Runs the specified test immediately";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 1 || arguments.size() == 2;
    }
}
