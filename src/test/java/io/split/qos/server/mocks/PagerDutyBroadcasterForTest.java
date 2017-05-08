package io.split.qos.server.mocks;

import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.util.TestId;

public class PagerDutyBroadcasterForTest implements PagerDutyBroadcaster {
    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }

    @Override
    public void initialize(String serviceKey, String qosServerName) {

    }

    @Override
    public void incident(TestId testId, String details) throws Exception {

    }

    @Override
    public void resolve(TestId testId) throws Exception {

    }

}
