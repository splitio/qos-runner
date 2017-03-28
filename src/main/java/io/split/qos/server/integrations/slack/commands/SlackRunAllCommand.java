package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.SlackColors;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Runs all the tests of the QOS.
 */
public class SlackRunAllCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerBehaviour behaviour;

    private static final int CHUNK_SIZE = 50;
    private final SlackColors colors;
    private final QOSServerState state;

    @Inject
    public SlackRunAllCommand(
            SlackColors slackColors,
            QOSServerBehaviour behaviour,
            QOSServerState state,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.colors = Preconditions.checkNotNull(slackColors);
        this.state = Preconditions.checkNotNull(state);
        this.serverName = Preconditions.checkNotNull(serverName);
        this.behaviour = Preconditions.checkNotNull(behaviour);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        if (state.isPaused()) {
            behaviour.resume(messagePosted.getSender().getUserName());
        }
        List<Method> tests = behaviour.runAllNow();
        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        tests
                .stream()
                .map(Util::id)
                .forEach(test -> {
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    testAttachment
                            .setColor(colors.getWarning());
                    toBeAdded.add(testAttachment);
                });
        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, CHUNK_SIZE);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            String title = String.format("[%s] TESTS", serverName.toUpperCase());
            String text = String.format("Total Tests to Run %s, tests %s - %s",
                    tests.size(),
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
        return "[server-name (optional)] runall: Runs all the tests of the qos server immediately";
    }
}
