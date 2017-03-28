package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.SlackColors;

/**
 * Resumes the server, new tests are added to be run.
 */
@Singleton
public class SlackResumeCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerBehaviour behaviour;
    private final SlackColors colors;

    @Inject
    public SlackResumeCommand(
            SlackColors slackColors,
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.colors = slackColors;
        this.serverName = Preconditions.checkNotNull(serverName);
        this.behaviour = behaviour;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        behaviour.resume(messagePosted.getSender().getUserName());
        String title = String.format("[%s] RESUME", serverName.toUpperCase());
        String text = "Server RESUMED by " + messagePosted.getSender().getUserName();

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor(colors.getInfo());

        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] resume: Resumes the tests of the server";
    }
}
