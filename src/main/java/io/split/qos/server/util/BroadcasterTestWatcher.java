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
import io.split.qos.server.integrations.IntegrationTestFactory;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import io.split.testrunner.util.GuiceInitializator;
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
            IntegrationTestFactory integrationTestFactory = QOSServerApplication.injector.getInstance(IntegrationTestFactory.class);
            Long length = null;
            state.testSucceeded(description);
            TestId testId = TestId.fromDescription(description);
            if (started.get(testId) != null) {
                length = System.currentTimeMillis() - started.get(testId);
            }
            Broadcast broadcast = failCondition.success(testId);
            SlackBroadcaster slack = integrationTestFactory.slackBroadcastIntegration();
            if (slack.isEnabled()) {
                if (Broadcast.RECOVERY.equals(broadcast)) {
                    slack.recovery(description, serverName, length, titleLink);
                }
                slack.success(description, serverName, length, titleLink);
            }
            PagerDutyBroadcaster pagerDuty = integrationTestFactory.pagerDutyBroadcaster();
            if (pagerDuty.isEnabled()) {
                if (Broadcast.RECOVERY.equals(broadcast)) {
                    try {
                        pagerDuty.resolve(testId);
                    } catch (Exception e) {
                        LOG.error(String.format("Failed to resolve pager duty for test %s", testId), e);
                    }
                }
            }
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        if (GuiceInitializator.isQos()) {
            IntegrationTestFactory integrationTestFactory = QOSServerApplication.injector.getInstance(IntegrationTestFactory.class);
            Long length = null;
            TestId testId = TestId.fromDescription(description);
            if (started.get(testId) != null) {
                length = System.currentTimeMillis() - started.get(testId);
            }
            Broadcast broadcast = failCondition.failed(testId);
            SlackBroadcaster slack = integrationTestFactory.slackBroadcastIntegration();
            if (slack.isEnabled()) {
                if (Broadcast.FIRST.equals(broadcast)) {
                    state.testFailed(description);
                    slack.firstFailure(description, e, serverName, length, titleLink);
                }
                if (Broadcast.REBROADCAST.equals(broadcast)) {
                    slack.reBroadcastFailure(description, e, serverName, failCondition.firstFailure(testId), length, titleLink);
                }
            }
            PagerDutyBroadcaster pagerDuty = integrationTestFactory.pagerDutyBroadcaster();
            if (pagerDuty.isEnabled()) {
                if (Broadcast.FIRST.equals(broadcast)) {
                    try {
                        pagerDuty.incident(testId, e.getLocalizedMessage());
                    } catch (Exception failed) {
                        LOG.error(String.format("Failed to trigger pager duty for test %s", testId), failed);
                    }
                }
            }
        }
    }

    @Override
    protected void starting(Description description) {
        if (GuiceInitializator.isQos()) {
            IntegrationTestFactory integrationTestFactory = QOSServerApplication.injector.getInstance(IntegrationTestFactory.class);
            started.put(TestId.fromDescription(description), System.currentTimeMillis());
            SlackBroadcaster slack = integrationTestFactory.slackBroadcastIntegration();
            if (slack.isEnabled()) {
                slack.initialize();
            }
        }
    }

    @Override
    protected void finished(Description description) {
        if (GuiceInitializator.isQos()) {
            IntegrationTestFactory integrationTestFactory = QOSServerApplication.injector.getInstance(IntegrationTestFactory.class);
            SlackBroadcaster slack = integrationTestFactory.slackBroadcastIntegration();
            if (slack.isEnabled()) {
                try {
                    slack.close();
                } catch (Exception e) {
                    LOG.error("Could not shutdown slack integration", e);
                }
            }
        }
    }
}
