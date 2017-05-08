package io.split.qos.server.integrations.pagerduty;

import io.split.qos.server.integrations.Integration;

public interface PagerDutyBroadcaster extends AutoCloseable, Integration {
    void initialize(String serviceKey, String qosServerName);

    void incident(String description, String details) throws Exception;
}
