package io.split.qos.server.integrations.slack.broadcaster;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import io.split.qos.server.integrations.slack.SlackBolt;
import io.split.qos.server.integrations.slack.SlackSessionProvider;
import io.split.testrunner.util.DateFormatter;
import org.junit.runner.Description;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.Blocks.section;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;

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
        broadcastFailure(description, error, slackSessionProvider.digestChannel(), serverName, duration, titleLink);
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
    public void broadcastVerbose(String message) {
        SlackBolt.sendMessage(message, slackSessionProvider.verboseChannel());
    }

    @Override
    public void broadcastDigest(String message) {
        SlackBolt.sendMessage(message,slackSessionProvider.digestChannel());
    }


    @Override
    public boolean isEnabled() {
        return slackSessionProvider.isEnabled();
    }

    @Override
    public void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure,
                                   Long duration, Optional<String> titleLink) {
        reBroadcastFailure(description, error, slackSessionProvider.digestChannel(), serverName, whenFirstFailure, duration, titleLink);
    }

    private void reBroadcastFailure(Description description,
                                    Throwable error,
                                    String channel,
                                    String serverName,
                                    Long whenFirstFailure,
                                    Long duration,
                                    Optional<String> titleLink) {

        SlackBolt.sendMessage(asBlocks(
                // Header
                header( header -> header.text(
                        getHeader(serverName, " :x: KEEPS FAILING SINCE", dateFormatter.formatDate(whenFirstFailure)))
                ),
                // Test name and duration
                section(section -> section.text(getTestName(description, dateFormatter.formatHour(duration)))
                ),
                divider(),
                // Reason of the failure
                section(section -> section.text(markdownText(mt -> mt.text(
                        "*REASON* \n "+error.getMessage())))
                ),
                // Stacktrace
                section(section -> section.text(markdownText(mt -> mt.text(
                        "*STACKTRACE* "+ exception(error))))
                )
        ), channel);
    }

    private void broadcastRecovery(Description description,
                                   String serverName,
                                   Long duration,
                                   Optional<String> titleLink) {
        SlackBolt.sendMessage(
                asBlocks(
                        // Header
                        section(section -> section.text(getHeader(serverName,":white_check_mark: RECOVERED", " "))
                        ),
                        // Test name and duration
                        section(section -> section.text(getTestName(description, dateFormatter.formatHour(duration)))
                        ))
                ,slackSessionProvider.verboseChannel()
        );
    }

    private void broadcastSuccess(Description description,
                                  String serverName,
                                  Long duration,
                                  Optional<String> titleLink) {
        SlackBolt.sendMessage(
            asBlocks(
                // Header
                section(section -> section.text(getHeader(serverName,":white_check_mark: SUCCEEDED", " "))
                ),
                // Test name and duration
                section(section -> section.text(getTestName(description, dateFormatter.formatHour(duration))))
            )
            ,slackSessionProvider.verboseChannel()
        );
    }

    private void broadcastFailure(Description description,
                                  Throwable error,
                                  String channel,
                                  String serverName,
                                  Long duration,
                                  Optional<String> titleLink) {
        SlackBolt.sendMessage(asBlocks(
                // Header
                header( header -> header.text(
                        getHeader(serverName, ":x: FAILED", " "))
                ),
                // Test name and description
                section(section -> section.text(getTestName(description, dateFormatter.formatHour(duration)))
                ),
                divider(),
                // Reason of the failure
                section(section -> section.text(markdownText(mt -> mt.text(
                        "*REASON* \n "+error.getMessage())))
                ),
                // Stacktrace
                section(section -> section.text(markdownText(mt -> mt.text(
                        "*STACKTRACE* "+ exception(error))))
                )
        ), channel);
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

    // Format the header string as a plan text with emoji visibility as true
    private PlainTextObject getHeader(String serverName, String message, String date){
        return plainText(pt -> pt.emoji(true).text(
                String.format("[%s] %s %s",
                        serverName.toUpperCase(),
                        message,
                        date
                )));
    }
    // Returns the string formated with the test name and duration
    private MarkdownTextObject getTestName(Description description, String date) {
        return markdownText(mt -> mt.text(
                String.format("%s#%s finished in %s",
                        description.getClassName(),
                        description.getMethodName(),
                        date
                )));
    }
}
