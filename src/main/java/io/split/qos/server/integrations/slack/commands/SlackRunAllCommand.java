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

/**
 * Runs all the tests of the QOS.
 */
public class SlackRunAllCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerBehaviour behaviour;

    private static final int CHUNK_SIZE = 50;
    private final SlackColors colors;
    private final QOSServerState state;
    private final SlackCommandGetter commandGetter;
    private final SlackAttachmentPartitioner partitioner;

    @Inject
    public SlackRunAllCommand(
            SlackColors slackColors,
            QOSServerBehaviour behaviour,
            QOSServerState state,
            SlackCommandGetter slackCommandGetter,
            SlackAttachmentPartitioner slackAttachmentPartitioner,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.colors = Preconditions.checkNotNull(slackColors);
        this.state = Preconditions.checkNotNull(state);
        this.commandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.partitioner = Preconditions.checkNotNull(slackAttachmentPartitioner);
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
        SlackCommand slackCommand = commandGetter.get(messagePosted).get();
        partitioner.send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] runall: Runs all the tests of the qos server immediately";
    }
}
