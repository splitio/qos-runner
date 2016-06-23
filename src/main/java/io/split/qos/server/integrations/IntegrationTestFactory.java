package io.split.qos.server.integrations;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;

/**
 * Has the integrations needed by the tests.
 */
@Singleton
public class IntegrationTestFactory {

    private final SlackBroadcaster slackBroadcastIntegration;

    @Inject
    public IntegrationTestFactory(
            SlackBroadcaster slackBroadcastIntegration) {
        this.slackBroadcastIntegration = Preconditions.checkNotNull(slackBroadcastIntegration);
    }

    public SlackBroadcaster slackBroadcastIntegration() {
        return slackBroadcastIntegration;
    }
}
