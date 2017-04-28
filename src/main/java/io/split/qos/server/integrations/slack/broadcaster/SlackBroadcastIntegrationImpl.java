package io.split.qos.server.integrations.slack.broadcaster;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import io.split.qos.server.QOSServerConfiguration;
import io.split.qos.server.integrations.slack.AbstractSlackIntegration;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.testrunner.util.DateFormatter;
import org.junit.runner.Description;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Singleton
public class SlackBroadcastIntegrationImpl extends AbstractSlackIntegration implements SlackBroadcaster {

    private final boolean broadcastSuccess;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackBroadcastIntegrationImpl(
            QOSServerConfiguration configuration,
            @Named(QOSPropertiesModule.BROADCAST_SUCCESS) String broadcastSuccess,
            DateFormatter dateFormatter,
            SlackCommon slackCommon) {
        super(configuration.getSlack().getBotToken(),
                configuration.getSlack().getDigestChannel(),
                configuration.getSlack().getVerboseChannel(),
                slackCommon);
        this.broadcastSuccess = Boolean.valueOf(Preconditions.checkNotNull(broadcastSuccess));
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
    }

    @Override
    public void firstFailure(Description description, Throwable error, String serverName, Long duration,
                             Optional<String> titleLink) {
        if (digestEnabled()) {
            broadcastFailure(description, error, digestChannel(), serverName, duration, titleLink);
        }
    }

    @Override
    public void recovery(Description description, String serverName, Long duration, Optional<String> titleLink) {
        if (digestEnabled()) {
            broadcastRecovery(description, serverName, duration, titleLink);
        }
    }

    @Override
    public void success(Description description, String serverName, Long duration, Optional<String> titleLink) {
        if (verboseEnabled()) {
            if (broadcastSuccess || !this.isInitializedByServer()) {
                broadcastSuccess(description, serverName, duration, titleLink);
            }
        }
    }

    @Override
    public void broadcastVerbose(String message, SlackAttachment attachment) {
        if (verboseEnabled()) {
            slackSession()
                    .sendMessage(
                            verboseChannel(),
                            message,
                            attachment);
        }
    }

    @Override
    public void broadcastDigest(String message, SlackAttachment attachment) {
        if (digestEnabled()) {
            slackSession()
                    .sendMessage(
                            digestChannel(),
                            message,
                            attachment);
        }
    }

    @Override
    public void close() throws Exception {
        close(false);
    }

    @Override
    public void initialize() throws IOException {
        initialize(false);
    }

    @Override
    public void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure,
                                   Long duration, Optional<String> titleLink) {
        if (digestEnabled()) {
            reBroadcastFailure(description, error, digestChannel(), serverName, whenFirstFailure, duration, titleLink);
        }
    }

    private void reBroadcastFailure(Description description,
                                    Throwable error,
                                    SlackChannel channel,
                                    String serverName,
                                    Long whenFirstFailure,
                                    Long duration,
                                    Optional<String> titleLink) {
        String text = String.format("%s#%s finished in %s",
                                        description.getClassName(),
                                        description.getMethodName(),
                                        dateFormatter.formatHour(duration));
        String title = String.format("[%s] KEEPS FAILING SINCE %s",
                                            serverName.toUpperCase(),
                                            dateFormatter.formatDate(whenFirstFailure));

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("danger");
        titleLink.ifPresent(link -> slackAttachment.setTitleLink(link));

        slackSession()
                .sendMessage(channel,
                        "",
                        slackAttachment);

        title = "Reason";
        text = error.getMessage();
        SlackAttachment reason = new SlackAttachment(title, "", text, null);
        reason
                .setColor("warning");
        slackSession()
                .sendMessage(channel,
                        "",
                        reason);

        StringBuilder exception = new StringBuilder();
        exception.append("```");
        Arrays.asList(error.getStackTrace())
                .stream()
                .forEach(stackTraceElement -> exception
                        .append(stackTraceElement.toString())
                        .append("\n"));
        exception.append("```");
        slackSession()
                .sendMessage(channel,
                        exception.toString());

    }

    private void broadcastRecovery(Description description,
                                   String serverName,
                                   Long duration,
                                   Optional<String> titleLink) {
        String text = String.format("%s#%s finished in %s", description.getClassName(), description.getMethodName(),
                dateFormatter.formatHour(duration));
        String title = String.format("[%s] RECOVERED", serverName.toUpperCase());

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("good");
        titleLink.ifPresent(link -> slackAttachment.setTitleLink(link));

        slackSession()
                .sendMessage(
                        digestChannel(),
                        "",
                        slackAttachment);

    }

    private void broadcastSuccess(Description description,
                                  String serverName,
                                  Long duration,
                                  Optional<String> titleLink) {
        slackSession()
                .sendMessage(
                        verboseChannel(),
                        "",
                        createHeaderAttachment(description, true, serverName, duration, titleLink));
    }

    private void broadcastFailure(Description description,
                                  Throwable error,
                                  SlackChannel channel,
                                  String serverName,
                                  Long duration,
                                  Optional<String> titleLink) {
        slackSession()
                .sendMessage(channel,
                        "",
                        createHeaderAttachment(description, false, serverName, duration, titleLink));

        String title = "Reason";
        String text = error.getMessage();
        SlackAttachment reason = new SlackAttachment(title, "", text, null);
        reason
                .setColor("warning");
        slackSession()
                .sendMessage(channel,
                        "",
                        reason);

        StringBuilder exception = new StringBuilder();
        exception.append("```");
        Arrays.asList(error.getStackTrace())
                .stream()
                .forEach(stackTraceElement -> exception
                        .append(stackTraceElement.toString())
                        .append("\n"));
        exception.append("```");
        slackSession()
                .sendMessage(channel,
                        exception.toString());
    }

    private SlackAttachment createHeaderAttachment(Description description,
                                                   boolean succeeded,
                                                   String serverName,
                                                   Long duration,
                                                   Optional<String> titleLink) {
        String text = String.format("%s#%s finished in %s", description.getClassName(), description.getMethodName(),
                dateFormatter.formatHour(duration));
        String title = String.format("[%s] %s", serverName.toUpperCase(), succeeded ? "SUCCEEDED" : "FAILED");

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor(succeeded ? "good" : "danger");
        titleLink
                .ifPresent(link -> slackAttachment.setTitleLink(link));
        return slackAttachment;
    }


}
