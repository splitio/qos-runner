package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.SlackColors;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

public class SlackRunTestCommand implements SlackCommandExecutor {
    private static final String TITLE = "[%s] RUN TEST";

    private final String serverName;
    private final QOSServerBehaviour behaviour;
    private final SlackCommandGetter slackCommandGetter;
    private final SlackColors colors;

    @Inject
    public SlackRunTestCommand(
            SlackColors slackColors,
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandGetter slackCommandGetter) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = slackColors;
        this.behaviour = behaviour;
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = slackCommandGetter.get(messagePosted).get();
        List<String> arguments = slackCommand.arguments2();
        if (arguments == null || arguments.isEmpty()) {
            slackEmpty(messagePosted, session);
            return false;
        }
        Optional<Method> methodExecuted;
        if (arguments.size() == 1) {
            methodExecuted = behaviour.runTestNow(Optional.empty(), arguments.get(0));
        } else {
            methodExecuted = behaviour.runTestNow(Optional.of(arguments.get(0)), arguments.get(1));
        }
        if (!methodExecuted.isPresent()) {
            String title = String.format(TITLE, serverName.toUpperCase());
            String text = String.format("Could not find test %s", arguments);

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor(colors.getFailed());
            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);
        } else {
            Method method = methodExecuted.get();
            String title = String.format(TITLE, serverName.toUpperCase());
            String text = String.format("Test Started %s", Util.id(method));

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor(colors.getSuccess());
            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);
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
        return "run [server-name] [class name (optional)] [test name]: Runs the specified test immediately";
    }
}
