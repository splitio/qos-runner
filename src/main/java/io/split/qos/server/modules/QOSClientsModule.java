package io.split.qos.server.modules;

import com.google.inject.AbstractModule;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcasterImpl;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcastIntegrationImpl;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;

public class QOSClientsModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(SlackBroadcaster.class).to(SlackBroadcastIntegrationImpl.class);
        bind(PagerDutyBroadcaster.class).to(PagerDutyBroadcasterImpl.class);
    }
}
