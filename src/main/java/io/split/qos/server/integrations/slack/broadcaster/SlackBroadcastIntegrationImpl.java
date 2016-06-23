package io.split.qos.server.integrations.slack.broadcaster;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import io.split.qos.server.failcondition.Broadcast;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.integrations.slack.AbstractSlackIntegration;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.util.DateFormatter;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.Optional;

@Singleton
public class SlackBroadcastIntegrationImpl extends AbstractSlackIntegration implements SlackBroadcaster {


    private final boolean enabled;
    private final boolean broadcastSuccess;
    private final DateFormatter dateFormatter;
    private final FailCondition failCondition;

    @Inject
    public SlackBroadcastIntegrationImpl(
            @Named(QOSPropertiesModule.SLACK_INTEGRATION) String slackIntegration,
            @Named(QOSPropertiesModule.SLACK_BOT_TOKEN) String slackBotToken,
            @Named(QOSPropertiesModule.SLACK_DIGEST_CHANNEL) String slackDigestChannel,
            @Named(QOSPropertiesModule.SLACK_VERBOSE_CHANNEL) String slackVerboseChannel,
            @Named(QOSPropertiesModule.BROADCAST_SUCCESS) String broadcastSuccess,
            FailCondition failCondition,
            DateFormatter dateFormatter,
            SlackCommon slackCommon) {
        super(slackBotToken, slackDigestChannel, slackVerboseChannel, slackCommon);
        this.enabled = Boolean.valueOf(Preconditions.checkNotNull(slackIntegration));
        this.broadcastSuccess = Boolean.valueOf(Preconditions.checkNotNull(broadcastSuccess));
        this.failCondition = Preconditions.checkNotNull(failCondition);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
    }

    /**
     * Logic is something like:
     * <ul>
     *     <li> If it reached the number of consecutive FAILURES -> Broadcast</li>
     *     <li> If it has been failing for more than reBroadcastFailureMinutes -> Broadcast</li>
     *     <li> If it failed and was not triggered by the QOS Server, meaning it was run as an individual test -> Broadcast</li>
     * </ul>
     */
    @Override
    public void failure(Description description, Throwable error, String serverName, Long duration) {
        Broadcast broadcast = failCondition.failed(description);
        if (Broadcast.FIRST.equals(broadcast)) {
            broadcastFailure(description, error, digestChannel(), serverName, duration);
        }
        if (Broadcast.REBROADCAST.equals(broadcast)) {
            reBroadcastFailure(description, error, digestChannel(), serverName, duration);
        }
        if (verboseEnabled()) {
            broadcastFailure(description, error, verboseChannel(), serverName, duration);
        }
    }

    /**
     * Simple when it recovers -> Broadcast.
     */
    @Override
    public void recovery(Description description, String serverName, Long duration) {
        if (digestEnabled()) {
            broadcastRecovery(description, serverName, duration);
        }
    }

    /**
     * Logic is something like:
     * <ul>
     *     <li> If it was failing before -> Broadcast Recovery</li>
     *     <li> If broadcastSuccess is enabled -> Broadcast Success</li>
     * </ul>
     */
    @Override
    public void success(Description description, String serverName, Long duration) {
        Broadcast success = failCondition.success(description);
        //It was failing before so it needs to broadcast success
        if (digestEnabled() && Broadcast.RECOVERY.equals(success)) {
                recovery(description, serverName, duration);
        }
        if (verboseEnabled()) {
            if (broadcastSuccess || !this.isInitializedByServer()) {
                broadcastSuccess(description, serverName, duration);
            }
        }
    }

    @Override
    public void broadcastVerbose(String message, SlackAttachment attachment) {
        if (isEnabled() && verboseEnabled()) {
            slackSession()
                    .sendMessage(
                            verboseChannel(),
                            message,
                            attachment);
        }
    }

    @Override
    public void broadcastDigest(String message, SlackAttachment attachment) {
        if (isEnabled() && digestEnabled()) {
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
    public void initialize() {
        initialize(false);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    private void reBroadcastFailure(Description description,
                                    Throwable error,
                                    SlackChannel channel,
                                    String serverName,
                                    Long duration) {
        Optional<Long> when = failCondition.firstFailure(description);
        String text = String.format("%s#%s finished in %s",
                                        description.getClassName(),
                                        description.getMethodName(),
                                        dateFormatter.formatHour(duration));
        String title = String.format("[%s] KEEPS FAILING SINCE %s",
                                            serverName.toUpperCase(),
                                            dateFormatter.formatDate(when.isPresent() ? when.get() : null));

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("danger");

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
                                   Long duration) {
        String text = String.format("%s#%s finished in %s", description.getClassName(), description.getMethodName(),
                dateFormatter.formatHour(duration));
        String title = String.format("[%s] RECOVERED", serverName.toUpperCase());

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor("good");

        slackSession()
                .sendMessage(
                        digestChannel(),
                        "",
                        slackAttachment);

    }

    private void broadcastSuccess(Description description,
                                  String serverName,
                                  Long duration) {
        slackSession()
                .sendMessage(
                        verboseChannel(),
                        "",
                        createHeaderAttachment(description, true, serverName, duration));
    }

    private void broadcastFailure(Description description,
                                  Throwable error,
                                  SlackChannel channel,
                                  String serverName,
                                  Long duration) {
        slackSession()
                .sendMessage(channel,
                        "",
                        createHeaderAttachment(description, false, serverName, duration));

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
                                                   Long duration) {
        String text = String.format("%s#%s finished in %s", description.getClassName(), description.getMethodName(),
                dateFormatter.formatHour(duration));
        String title = String.format("[%s] %s", serverName.toUpperCase(), succeeded ? "SUCCEEDED" : "FAILED");

        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor(succeeded ? "good" : "danger");
        return slackAttachment;
    }


}
