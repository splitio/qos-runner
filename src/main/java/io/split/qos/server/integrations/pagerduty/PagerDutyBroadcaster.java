package io.split.qos.server.integrations.pagerduty;

import io.split.qos.server.integrations.Integration;
import io.split.qos.server.util.TestId;

public interface PagerDutyBroadcaster extends AutoCloseable, Integration {
    void initialize(String serviceKey, String qosServerName);

    void incident(TestId testId, String details) throws Exception;

    void resolve(TestId testId) throws Exception;
}
