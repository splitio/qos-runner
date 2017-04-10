package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;

/**
 * Slacks some info about the server.
 */
@Singleton
public class SlackInfoCommand extends SlackAbstractCommand {

    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final String suites;
    private final String description;

    @Inject
    public SlackInfoCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            @Named(QOSPropertiesModule.SUITES) String suites,
            @Named(QOSPropertiesModule.DESCRIPTION) String description) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.state = state;
        this.description = Preconditions.checkNotNull(description);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.suites = Preconditions.checkNotNull(suites);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        String color = state.isActive() ? colors().getSuccess() : colors().getWarning();

        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        if (!Strings.isNullOrEmpty(description)) {
            SlackAttachment descriptionAttachment = new SlackAttachment("Description", "", description, null);
            descriptionAttachment.setColor(color);
            toBeAdded.add(descriptionAttachment);
        }
        if (state.isActive()) {
            String text = String.format("%s (since %s)", state.status(), dateFormatter.formatDate(state.activeSince()));
            SlackAttachment statusAttachment = new SlackAttachment("Status", "", text, null);
            statusAttachment.setColor(color);
            toBeAdded.add(statusAttachment);
        } else {
            String text = String.format("%s (since %s)", state.status(), dateFormatter.formatDate(state.pausedSince()));
            SlackAttachment statusAttachment = new SlackAttachment("Status", "", text, null);
            statusAttachment.setColor(color);
            toBeAdded.add(statusAttachment);
        }
        SlackAttachment suitesAttachment = new SlackAttachment("Suites", "", suites, null);
        suitesAttachment.setColor(color);
        toBeAdded.add(suitesAttachment);
        SlackAttachment lastTestFinishedAttachment = new SlackAttachment("Last test finished", "",
                dateFormatter.formatDate(state.lastTestFinished()), null);
        lastTestFinishedAttachment .setColor(color);
        toBeAdded.add(lastTestFinishedAttachment);

        if (state.isActive()) {
            SlackAttachment resumedByAttachment = new SlackAttachment("Resumed by", "", state.who(), null);
            resumedByAttachment.setColor(color);
            toBeAdded.add(resumedByAttachment);
        } else {
            SlackAttachment resumedByAttachment = new SlackAttachment("Paused by", "", state.who(), null);
            resumedByAttachment.setColor(color);
            toBeAdded.add(resumedByAttachment);
        }
        messageSender()
                .send(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
        return true;
    }

    @Override
    public String description() {
        return "Displays Server Info, like status, time since status change, last test ran";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] info";
    }


    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
