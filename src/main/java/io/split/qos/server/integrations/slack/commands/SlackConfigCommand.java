package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Lists the configuration of the server (basically listing the properties file).
 */
public class SlackConfigCommand extends SlackAbstractCommand {

    private final Properties configuration;

    @Inject
    public SlackConfigCommand(
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender messageSender,
            @Named(QOSPropertiesModule.CONFIGURATION) Properties configuration,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        super(slackColors, serverName, messageSender, slackCommandGetter);
        this.configuration = Preconditions.checkNotNull(configuration);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand command = command(messagePosted);
        List<String> confs = configuration
                .entrySet()
                .stream()
                .map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        messageSender()
                .sendInfo(command.command(), String.join("\n", confs), messagePosted.getChannel(), session);
        return true;
    }

    @Override
    public String help() {
        return "[server-name (optional)] config: Displays a lists of the configured properties";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }

}
