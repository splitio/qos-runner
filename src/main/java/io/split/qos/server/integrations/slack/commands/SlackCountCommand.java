package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
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
import io.split.qos.server.util.TestId;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.Map;

@Singleton
public class SlackCountCommand extends SlackAbstractCommand {

    private final DateFormatter dateFormatter;
    private final String suites;
    private final String description;
    private final QOSServerState state;

    @Inject
    public SlackCountCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            @Named(QOSPropertiesModule.SUITES) String suites,
            @Named(QOSPropertiesModule.DESCRIPTION) String description) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.state = Preconditions.checkNotNull(state);
        this.description = Preconditions.checkNotNull(description);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.suites = Preconditions.checkNotNull(suites);
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        Map<TestId, QOSServerState.TestStatus> failed = state.failedTests();
        Map<TestId, QOSServerState.TestStatus> missing = state.missingTests();
        Map<TestId, QOSServerState.TestStatus> succeeded = state.succeededTests();
        List<SlackAttachment> toSend = Lists.newArrayList();
        SlackAttachment succeededAttachment = new SlackAttachment("Succeeded: " + succeeded.size(), "", "", null);
        succeededAttachment.setColor(colors().getSuccess());
        toSend.add(succeededAttachment);

        SlackAttachment failedAttachment = new SlackAttachment("Failed: " + failed.size(), "", "", null);
        failedAttachment.setColor(colors().getFailed());
        toSend.add(failedAttachment);

        SlackAttachment missingAttachment = new SlackAttachment("Missing: " + missing.size(), "", "", null);
        missingAttachment.setColor(colors().getWarning());

        toSend.add(missingAttachment);
        messageSender()
            .send(slackCommand.command(), "Total Tests: " + (failed.size() + missing.size() + succeeded.size()),session, messagePosted.getChannel(), toSend);
        return true;
    }

    @Override
    public String description() {
        return "Displays a count of the succeeded, failed and missing tests";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] count";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 0;
    }
}
