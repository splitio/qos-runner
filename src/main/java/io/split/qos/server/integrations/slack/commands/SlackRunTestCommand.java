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
import io.split.qos.server.util.SlackAttachmentPartitioner;
import io.split.testrunner.util.SlackColors;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class SlackRunTestCommand implements SlackCommandExecutor {
    private static final String TITLE = "[%s] RUN TEST";
    private static final int CHUNK_SIZE = 50;

    private final String serverName;
    private final QOSServerBehaviour behaviour;
    private final SlackCommandGetter slackCommandGetter;
    private final SlackColors colors;
    private final QOSServerState state;
    private final SlackAttachmentPartitioner partitioner;

    @Inject
    public SlackRunTestCommand(
            SlackColors slackColors,
            SlackAttachmentPartitioner slackAttachmentPartitioner,
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandGetter slackCommandGetter,
            QOSServerState state) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = slackColors;
        this.partitioner = Preconditions.checkNotNull(slackAttachmentPartitioner);
        this.behaviour = behaviour;
        this.state = Preconditions.checkNotNull(state);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = slackCommandGetter.get(messagePosted).get();
        List<String> arguments = slackCommand.arguments();
        if (arguments == null || arguments.isEmpty()) {
            slackEmpty(messagePosted, session);
            return false;
        }
        List<Method> methodsExecuted;
        if (arguments.size() == 1) {
            methodsExecuted = behaviour.runTestsNow(Optional.empty(), arguments.get(0));
        } else {
            methodsExecuted = behaviour.runTestsNow(Optional.of(arguments.get(0)), arguments.get(1));
        }
        if (methodsExecuted.isEmpty()) {
            String title = String.format(TITLE, serverName.toUpperCase());
            String text = String.format("Could not find tests %s", arguments);

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor(colors.getFailed());
            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);
        } else {
            List<SlackAttachment> toBeAdded = Lists.newArrayList();
            methodsExecuted
                    .stream()
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
            partitioner.send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
            return true;
        }
        return false;
    }

    private void slackEmpty(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format(TITLE, serverName.toUpperCase());
        String text = "Run Test needs an actual test to run";
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
        return "[server-name (optional)] run [class name (optional)] [test name]: Runs the specified test immediately";
    }
}
