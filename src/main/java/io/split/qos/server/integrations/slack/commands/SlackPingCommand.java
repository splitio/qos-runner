package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.SlackColors;

/**
 * Created on 11/1/16.
 */
@Singleton
public class SlackPingCommand implements SlackCommandExecutor {

    private final String serverName;
    private final SlackColors colors;

    @Inject
    public SlackPingCommand(
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.colors = slackColors;
        this.serverName = Preconditions.checkNotNull(serverName);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format("[%s] PONG", serverName.toUpperCase());
        SlackAttachment slackAttachment = new SlackAttachment(title, "", "", "");
        slackAttachment.setColor(colors.getInfo());
        SlackPreparedMessage sent = new SlackPreparedMessage
                .Builder()
                .addAttachment(slackAttachment)
                .build();
        session
                .sendMessage(messagePosted.getChannel(), sent);
        return true;
    }

    @Override
    public String help() {
        return "ping [server-name]: Server replies with a pong";
    }

}