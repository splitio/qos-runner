package io.split.qos.server.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.failcondition.Broadcast;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.integrations.IntegrationTestFactory;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;

/**
 * Rule for broadcasting success/failures.
 */
@Singleton
public class BroadcasterTestWatcher extends TestWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcasterTestWatcher.class);

    private final SlackBroadcaster slack;
    public static String serverName = "NOT INITIALIZED";
    private static final Map<TestId, Long> started = Maps.newConcurrentMap();
    private final FailCondition failCondition;
    private final QOSServerState state;
    private final PagerDutyBroadcaster pagerDuty;
    //Hack to set the title link at runtime
    private Optional<String> titleLink;

    @Inject
    public BroadcasterTestWatcher(
            IntegrationTestFactory integrationTestFactory,
            FailCondition failCondition,
            QOSServerState state) {
        this.slack = Preconditions.checkNotNull(integrationTestFactory).slackBroadcastIntegration();
        this.pagerDuty = Preconditions.checkNotNull(integrationTestFactory).pagerDutyBroadcaster();
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
        Long length = null;
        state.testSucceeded(description);
        if (started.get(TestId.fromDescription(description)) != null) {
            length = System.currentTimeMillis() - started.get(TestId.fromDescription(description));
        }
        Broadcast broadcast = failCondition.success(TestId.fromDescription(description));
        if (slack.isEnabled()) {
            if (Broadcast.RECOVERY.equals(broadcast)) {
                slack.recovery(description, serverName, length, titleLink);
            }
            slack.success(description, serverName, length, titleLink);
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        Long length = null;
        TestId testId = TestId.fromDescription(description);
        if (started.get(testId) != null) {
            length = System.currentTimeMillis() - started.get(testId);
        }
        Broadcast broadcast = failCondition.failed(testId);
        if (slack.isEnabled()) {
            if (Broadcast.FIRST.equals(broadcast)) {
                state.testFailed(description);
                slack.firstFailure(description, e, serverName, length, titleLink);
            }
            if (Broadcast.REBROADCAST.equals(broadcast)) {
                slack.reBroadcastFailure(description, e, serverName, failCondition.firstFailure(testId), length, titleLink);
            }
        }
        if (pagerDuty.isEnabled()) {
            if (Broadcast.FIRST.equals(broadcast)) {
                try {
                    pagerDuty.incident(testId.toString(), e.getLocalizedMessage());
                } catch (Exception failed) {
                    LOG.error("Failed to trigger pager duty", failed);
                }
            }
        }
    }

    @Override
    protected void starting(Description description) {
        started.put(TestId.fromDescription(description), System.currentTimeMillis());
        if (slack.isEnabled()) {
            slack.initialize();
        }
    }

    @Override
    protected void finished(Description description) {
        if (slack.isEnabled()) {
            try {
                slack.close();
            } catch (Exception e) {
                LOG.error("Could not shutdown slack integration", e);
            }
        }
    }
}
