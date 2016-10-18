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
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Runs all the tests of the QOS.
 */
public class SlackRunAllCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerBehaviour behaviour;

    private static final int CHUNK_SIZE = 100;

    @Inject
    public SlackRunAllCommand(
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.behaviour = behaviour;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        List<Method> tests = behaviour.runAllNow();
        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        tests
                .stream()
                .map(Util::id)
                .forEach(test -> {
                    SlackAttachment testAttachment = new SlackAttachment("", "", test, null);
                    testAttachment
                            .setColor("good");
                    toBeAdded.add(testAttachment);
                });
        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, 10);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            String title = String.format("[%s] Tests", serverName.toUpperCase());
            String text = String.format("Total Tests to Run %s, tests %s - %s", tests.size(), 1 + CHUNK_SIZE * iteration, CHUNK_SIZE * (iteration+1));

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor("good");

            SlackPreparedMessage.Builder partitionSend = new SlackPreparedMessage
                    .Builder()
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
        return "runall [server-name]: Runs all the tests of the qos server immediately";
    }
}
