package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
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
        List<SlackAttachment> confs = configuration
                .entrySet()
                .stream()
                .map(entry -> {
                    SlackAttachment result = new SlackAttachment(entry.getKey().toString(), "", entry.getValue().toString(), null);
                    result.setColor(colors().getInfo());
                    return result;
                })
                .sorted((o1, o2) -> {
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.getTitle().compareTo(o2.getTitle());
                })
                .collect(Collectors.toList());
        messageSender()
                .sendPartition(command.command(), session, messagePosted.getChannel(), confs);
        return true;
    }

    @Override
    public String description() {
        return "Displays a lists of the configured properties";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] config";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }

}
