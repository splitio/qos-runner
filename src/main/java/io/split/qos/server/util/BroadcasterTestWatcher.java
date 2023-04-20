package io.split.qos.server.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.QOSServerApplication;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.failcondition.Broadcast;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.integrations.datadog.DatadogBroadcaster;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.broadcaster.SlackTestResultBroadcaster;
import io.split.testrunner.util.GuiceInitializator;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Rule for broadcasting success/failures.
 */
@Singleton
public class BroadcasterTestWatcher extends TestWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcasterTestWatcher.class);

    public static String serverName = "NOT INITIALIZED";
    private static final Map<TestId, Long> started = Maps.newConcurrentMap();
    private final FailCondition failCondition;
    private final QOSServerState state;
    //Hack to set the title link at runtime
    private Optional<String> titleLink;

    @Inject
    public BroadcasterTestWatcher(
            FailCondition failCondition,
            QOSServerState state) {
        this.failCondition = Preconditions.checkNotNull(failCondition);
        this.state = Preconditions.checkNotNull(state);
        this.titleLink = Optional.empty();
    }

    public void setTitleLink(String titleLink) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(titleLink));
        this.titleLink = Optional.of(titleLink);
    }

    @Override
    protected void succeeded(Description description) {
        if (GuiceInitializator.isQos()) {
            SlackTestResultBroadcaster slackBroadcaster = QOSServerApplication.injector.getInstance(SlackTestResultBroadcaster.class);
            Long length = null;
            state.testSucceeded(description);
            TestId testId = TestId.fromDescription(description);
            if (started.get(testId) != null) {
                length = System.currentTimeMillis() - started.get(testId);
            }
            Broadcast broadcast = failCondition.success(testId);
            if (slackBroadcaster.isEnabled()) {
                if (Broadcast.RECOVERY.equals(broadcast)) {
                    slackBroadcaster.recovery(description, serverName, length, titleLink);
                }
                slackBroadcaster.success(description, serverName, length, titleLink);
            }

            // Pagerduty
            PagerDutyBroadcaster pagerDuty = QOSServerApplication.injector.getInstance(PagerDutyBroadcaster.class);
            if (pagerDuty.isEnabled()) {
                if (Broadcast.RECOVERY.equals(broadcast)) {
                    try {
                        pagerDuty.resolve(testId);
                    } catch (Exception e) {
                        LOG.error(String.format("Failed to resolve pager duty for test %s", testId), e);
                    }
                }
            }

            // Datadog
            DatadogBroadcaster datadog = QOSServerApplication.injector.getInstance(DatadogBroadcaster.class);
            if (datadog.isEnabled()) {
                if (Broadcast.RECOVERY.equals(broadcast)) {
                    datadog.recovery(description, serverName, length, titleLink);
                }
                datadog.success(description, serverName, length, titleLink);
            }

        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (GuiceInitializator.isQos()) {
            Long length = null;
            TestId testId = TestId.fromDescription(description);
            if (started.get(testId) != null) {
                length = System.currentTimeMillis() - started.get(testId);
            }

            // Slack
            Broadcast broadcast = failCondition.failed(testId);
            SlackTestResultBroadcaster resultBroadcaster = QOSServerApplication.injector.getInstance(SlackTestResultBroadcaster.class);
            if (resultBroadcaster.isEnabled()) {
                if (Broadcast.FIRST.equals(broadcast)) {
                    state.testFailed(description);
                    resultBroadcaster.firstFailure(description, e, serverName, length, titleLink);
                }
                if (Broadcast.REBROADCAST.equals(broadcast)) {
                    resultBroadcaster.reBroadcastFailure(description, e, serverName, failCondition.firstFailure(testId), length, titleLink);
                }
            }

            // Pagerduty
            PagerDutyBroadcaster pagerDuty = QOSServerApplication.injector.getInstance(PagerDutyBroadcaster.class);
            if (pagerDuty.isEnabled()) {
                if (Broadcast.FIRST.equals(broadcast)) {
                    try {
                        pagerDuty.incident(testId, e.getLocalizedMessage());
                    } catch (Exception failed) {
                        LOG.error(String.format("Failed to trigger pager duty for test %s", testId), failed);
                    }
                }
            }

            // Datadog
            DatadogBroadcaster datadog = QOSServerApplication.injector.getInstance(DatadogBroadcaster.class);
            if (resultBroadcaster.isEnabled()) {
                if (Broadcast.FIRST.equals(broadcast)) {
                    datadog.firstFailure(description, e, serverName, length, titleLink);
                }
                if (Broadcast.REBROADCAST.equals(broadcast)) {
                    datadog.reBroadcastFailure(description, e, serverName, failCondition.firstFailure(testId), length, titleLink);
                }
                // Send to Datadog every time it fails due to Sauce Labs
                String reason = e.getMessage();
                if (reason.contains("Could not start a new session.") || reason.contains("It is impossible to create a new session")) {
                    LOG.info(String.format("Sauce error: The test %s failed with reason: %s", description.getMethodName(), reason));
                    datadog.sauceFailure(description, serverName);
                }
                if (reason.contains("Impression Not Present KeyImpressionDTO")) {
                    String stacktrace = getStackTrace(e);
                    LOG.info(String.format("Impression Not Present error: The test %s failed with reason: %s. Stacktrace: %s", description.getMethodName(), reason, stacktrace));
                    datadog.impressionFailure(description, serverName);
                }
            }
        }
    }

    private String getStackTrace(Throwable error){
        StringBuilder exception = new StringBuilder();
        List<StackTraceElement> elements = Arrays.asList(error.getStackTrace());
        elements.stream()
                .map(stackTraceElement -> stackTraceElement.toString())
                .forEach(line -> exception
                        .append(line)
                        .append(" "));
        return exception.toString();
    }

    @Override
    protected void starting(Description description) {
        if (GuiceInitializator.isQos()) {
            started.put(TestId.fromDescription(description), System.currentTimeMillis());
        }
    }

    @Override
    protected void finished(Description description) {
        if (GuiceInitializator.isQos()) {
//            SlackTestResultBroadcaster resultBroadcaster = QOSServerApplication.injector.getInstance(SlackTestResultBroadcaster.class);
//            if (resultBroadcaster.isEnabled()) {
//                try {
//                    resultBroadcaster.close();
//                } catch (Exception e) {
//                    LOG.error("Could not shutdown slack integration", e);
//                }
//            }
        }
    }
}
