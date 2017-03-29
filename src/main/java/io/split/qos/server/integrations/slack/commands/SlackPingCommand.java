package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.SlackColors;

import java.util.List;

/**
 * Created on 11/1/16.
 */
@Singleton
public class SlackPingCommand extends SlackAbstractCommand {

    @Inject
    public SlackPingCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        messageSender()
                .sendInfo("PONG",
                        "",
                        messagePosted.getChannel(),
                        session);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] ping: Server replies with a pong";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}