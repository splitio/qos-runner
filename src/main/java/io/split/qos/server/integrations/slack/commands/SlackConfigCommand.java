package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Lists the configuration of the server (basically listing the properties file).
 */
public class SlackConfigCommand implements SlackCommandExecutor {

    private final Properties configuration;
    private final String serverName;
    private final SlackCommonFormatter formatter;

    @Inject
    public SlackConfigCommand(
            SlackCommonFormatter formatter,
            @Named(QOSPropertiesModule.CONFIGURATION) Properties configuration,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.configuration = Preconditions.checkNotNull(configuration);
        this.formatter = Preconditions.checkNotNull(formatter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format("[%s] Config QOS Server", serverName);

        SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
        slackAttachment
                .setColor("good");

        List<String> confs = configuration
                .entrySet()
                .stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        formatter
                .groupMessage(confs)
                .forEach(group -> session
                        .sendMessage(messagePosted.getChannel(),
                                group));

        return true;
    }

    @Override
    public String help() {
        return "config [server-name]: Displays a lists of the configured properties";
    }
}
