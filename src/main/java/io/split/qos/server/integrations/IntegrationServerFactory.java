package io.split.qos.server.integrations;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegration;

/**
 * Has the integrations needed by the Server.
 */
@Singleton
public class IntegrationServerFactory {

    private final SlackBroadcaster slackBroadcastIntegration;
    private final SlackCommandIntegration slackCommandIntegration;

    @Inject
    public IntegrationServerFactory(
            SlackBroadcaster slackBroadcastIntegration,
            SlackCommandIntegration slackCommandIntegration) {
        this.slackBroadcastIntegration = Preconditions.checkNotNull(slackBroadcastIntegration);
        this.slackCommandIntegration = Preconditions.checkNotNull(slackCommandIntegration);
    }

    public SlackBroadcaster slackBroadcastIntegration() {
        return slackBroadcastIntegration;
    }

    public SlackCommandIntegration slackCommandIntegration() {
        return slackCommandIntegration;
    }
}