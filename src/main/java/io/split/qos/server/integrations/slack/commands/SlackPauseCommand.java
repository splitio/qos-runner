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

/**
 * Pauses the server. No new tests will be added, currently running tests
 * will finish.
 */
@Singleton
public class SlackPauseCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerBehaviour behaviour;

    @Inject
    public SlackPauseCommand(
            QOSServerBehaviour behaviour,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.behaviour = behaviour;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        behaviour.pause(messagePosted.getSender().getUserName());
        String title = String.format("[%s] PAUSE", serverName.toUpperCase());
        String text = "Server PAUSED by " + messagePosted.getSender().getUserName();

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("warning");

        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        return true;
    }

    @Override
    public String help() {
        return "pause [server-name]: Pauses the tests running for the server. Current tests will keep running";
    }
}
