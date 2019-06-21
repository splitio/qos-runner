package io.split.qos.server.integrations.slack.broadcaster;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import io.split.qos.server.integrations.slack.SlackSessionProvider;
import io.split.testrunner.util.DateFormatter;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class SlackTestResultBroacasterImpl implements SlackTestResultBroadcaster {

    private final DateFormatter dateFormatter;
    private final SlackSessionProvider slackSessionProvider;

    @Inject
    public SlackTestResultBroacasterImpl(DateFormatter dateFormatter,
                                         SlackSessionProvider slackSessionProvider) {
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.slackSessionProvider = Preconditions.checkNotNull(slackSessionProvider);
    }

    @Override
    public void firstFailure(Description description, Throwable error, String serverName, Long duration,
                             Optional<String> titleLink) {
        if (errorEqualOrGreaterThanFiveHundred(error)) {
            broadcastFailure(description, error, slackSessionProvider.alertChannel(), serverName, duration, titleLink);
        }
        else {
            broadcastFailure(description, error, slackSessionProvider.digestChannel(), serverName, duration, titleLink);
        }
    }

    @Override
    public void recovery(Description description, String serverName, Long duration, Optional<String> titleLink) {
        broadcastRecovery(description, serverName, duration, titleLink);
    }

    @Override
    public void success(Description description, String serverName, Long duration, Optional<String> titleLink) {
        broadcastSuccess(description, serverName, duration, titleLink);
    }

    @Override
    public void broadcastVerbose(String message, SlackAttachment attachment) {
        slackSessionProvider.slackSession()
                    .sendMessage(
                            slackSessionProvider.verboseChannel(),
                            message,
                            attachment);
    }

    @Override
    public void broadcastDigest(String message, SlackAttachment attachment) {
        slackSessionProvider
                .slackSession()
                    .sendMessage(
                            slackSessionProvider.digestChannel(),
                            message,
                            attachment);
    }

    @Override
    public void broadcastAlert(String message, SlackAttachment attachment) {
        slackSessionProvider
                .slackSession()
                .sendMessage(
                        slackSessionProvider.alertChannel(),
                        message,
                        attachment);
    }


    @Override
    public boolean isEnabled() {
        return slackSessionProvider.isEnabled();
    }

    @Override
    public void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure,
                                   Long duration, Optional<String> titleLink) {
        if (errorEqualOrGreaterThanFiveHundred(error)) {
            reBroadcastFailure(description, error, slackSessionProvider.digestChannel(), serverName, whenFirstFailure, duration, titleLink);
        }
        else {
            reBroadcastFailure(description, error, slackSessionProvider.verboseChannel(), serverName, whenFirstFailure, duration, titleLink);
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

        slackSessionProvider
                .slackSession()
                .sendMessage(channel,
                        "",
                        slackAttachment);

        title = "Reason";
        text = error.getMessage();
        SlackAttachment reason = new SlackAttachment(title, "", text, null);
        reason
                .setColor("warning");

        slackSessionProvider
                .slackSession()
                .sendMessage(channel,
                        "",
                        reason);

        slackSessionProvider
                .slackSession()
                .sendMessage(channel,
                        exception(error));

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

        slackSessionProvider
                .slackSession()
                .sendMessage(
                        slackSessionProvider.digestChannel(),
                        "",
                        slackAttachment);

    }

    private void broadcastSuccess(Description description,
                                  String serverName,
                                  Long duration,
                                  Optional<String> titleLink) {
        slackSessionProvider
                .slackSession()
                .sendMessage(
                        slackSessionProvider.verboseChannel(),
                        "",
                        createHeaderAttachment(description, true, serverName, duration, titleLink));
    }

    private void broadcastFailure(Description description,
                                  Throwable error,
                                  SlackChannel channel,
                                  String serverName,
                                  Long duration,
                                  Optional<String> titleLink) {
        slackSessionProvider
                .slackSession()
                .sendMessage(channel,
                        "",
                        createHeaderAttachment(description, false, serverName, duration, titleLink));

        String title = "Reason";
        String text = error.getMessage();
        SlackAttachment reason = new SlackAttachment(title, "", text, null);
        reason
                .setColor("warning");
        slackSessionProvider
                .slackSession()
                .sendMessage(channel,
                        "",
                        reason);

        slackSessionProvider
                .slackSession()
                .sendMessage(channel,
                        exception(error));
    }

    private static final int MAX_EXCEPTION_LENGTH = 15;

    private String exception(Throwable error) {
        StringBuilder exception = new StringBuilder();
        exception.append("```");
        List<StackTraceElement> elements = Arrays.asList(error.getStackTrace());
        elements
                .stream()
                .map(stackTraceElement -> stackTraceElement.toString())
                .limit(Math.min(MAX_EXCEPTION_LENGTH, elements.size()))
                .forEach(line -> exception
                        .append(line)
                        .append("\n"));
        if (elements.size() > MAX_EXCEPTION_LENGTH) {
            exception.append("...\n");
        }
        exception.append("```");
        return exception.toString();

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

    private boolean errorEqualOrGreaterThanFiveHundred(Throwable error) {
        String reason = error.getMessage();
        Pattern pattern = Pattern.compile("50*");

        return pattern.matcher(reason).find();
    }
}
