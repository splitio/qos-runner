package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Runs all the tests of the QOS.
 */
public class SlackRunAllCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerBehaviour behaviour;
    private final SlackCommonFormatter formatter;

    @Inject
    public SlackRunAllCommand(
            QOSServerBehaviour behaviour,
            SlackCommonFormatter formatter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.behaviour = behaviour;
        this.formatter = Preconditions.checkNotNull(formatter);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        List<Method> tests = behaviour.runAllNow();
        String title = String.format("[%s] RUNNING ALL TESTS NOW", serverName);
        String text = String.format("Running now %s tests triggered by ", tests.size(), messagePosted.getSender().getUserName());

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("good");

        List<String> toBeRun = tests
                .stream()
                .map(value -> Util.id(value))
                .collect(Collectors.toList());

        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);

        formatter
                .groupMessage(toBeRun)
                .forEach(group -> session
                        .sendMessage(messagePosted.getChannel(),
                                group));
        return true;
    }

    @Override
    public String help() {
        return "runall [server-name]: Runs all the tests of the qos server immediately";
    }
}
