package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;

import java.util.Properties;

public class SlackConfigCommand implements SlackCommandExecutor {

    private final Properties configuration;
    private final String serverName;

    @Inject
    public SlackConfigCommand(
            @Named(QOSPropertiesModule.CONFIGURATION) Properties configuration,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.configuration = Preconditions.checkNotNull(configuration);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format("[%s] Config QOS Server", serverName);

        SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
        slackAttachment
                .setColor("good");

        StringBuilder configList = new StringBuilder();
        configList.append("```\n");
        configuration
                .entrySet()
                .stream()
                .forEach(entry -> configList.append(String.format("%s=%s \n", entry.getKey(), entry.getValue())));
        configList.append("```\n");
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        session
                .sendMessage(messagePosted.getChannel(),
                        configList.toString());

        return true;
    }

    @Override
    public String help() {
        return "config [server-name]: Displays a lists of the configured properties";
    }
}
