package io.split.qos.server.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.failcondition.Broadcast;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.integrations.IntegrationTestFactory;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Rule for broadcasting success/failures.
 */
@Singleton
public class BroadcasterTestWatcher extends TestWatcher {
    private static final Logger LOG = LoggerFactory.getLogger(BroadcasterTestWatcher.class);

    private final SlackBroadcaster slack;
    public static String serverName = "NOT INITIALIZED";
    private static final Map<String, Long> started = Maps.newConcurrentMap();
    private final FailCondition failCondition;
    private final QOSServerState state;

    @Inject
    public BroadcasterTestWatcher(
            IntegrationTestFactory integrationTestFactory,
            FailCondition failCondition,
            QOSServerState state) {
        this.slack = Preconditions.checkNotNull(integrationTestFactory).slackBroadcastIntegration();
        this.failCondition = Preconditions.checkNotNull(failCondition);
        this.state = Preconditions.checkNotNull(state);
    }

    @Override
    protected void succeeded(Description description) {
        Long length = null;
        state.testSucceeded(description);
        if (started.get(Util.id(description)) != null) {
            length = System.currentTimeMillis() - started.get(Util.id(description));
        }
        if (slack.isEnabled()) {
            slack.success(description, serverName, length);
        }
    }

    @Override
    protected void failed(Throwable e, Description description) {
        Long length = null;
        if (started.get(Util.id(description)) != null) {
            length = System.currentTimeMillis() - started.get(Util.id(description));
        }
        Broadcast broadcast = failCondition.failed(description);
        if (Broadcast.FIRST.equals(broadcast)) {
            state.testFailed(description);
            if (slack.isEnabled()) {
                slack.failure(description, e, serverName, length);
            }
        }
    }

    @Override
    protected void starting(Description description) {
        started.put(Util.id(description), System.currentTimeMillis());
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
