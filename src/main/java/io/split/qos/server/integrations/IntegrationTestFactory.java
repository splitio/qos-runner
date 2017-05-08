package io.split.qos.server.integrations;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;

/**
 * Has the integrations needed by the tests.
 */
@Singleton
public class IntegrationTestFactory {

    private final SlackBroadcaster slackBroadcastIntegration;
    private final PagerDutyBroadcaster pagerDutyBroadcaster;

    @Inject
    public IntegrationTestFactory(
            SlackBroadcaster slackBroadcastIntegration,
            PagerDutyBroadcaster pagerDutyBroadcaster) {
        this.slackBroadcastIntegration = Preconditions.checkNotNull(slackBroadcastIntegration);
        this.pagerDutyBroadcaster = Preconditions.checkNotNull(pagerDutyBroadcaster);
    }

    public SlackBroadcaster slackBroadcastIntegration() {
        return slackBroadcastIntegration;
    }

    public PagerDutyBroadcaster pagerDutyBroadcaster() {
        return pagerDutyBroadcaster;
    }

}
