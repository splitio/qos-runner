package io.split.qos.server.integrations.pagerduty;

import com.github.dikhan.PagerDutyEventsClient;
import com.github.dikhan.domain.TriggerIncident;
import com.github.dikhan.exceptions.NotifyEventException;
import com.google.common.base.Preconditions;
import com.google.inject.Singleton;

@Singleton
public class PagerDutyBroadcasterImpl implements PagerDutyBroadcaster {
    private String serviceKey;
    private String qosServerName;

    @Override
    public boolean isEnabled() {
        return serviceKey != null;
    }

    @Override
    public void close() throws Exception { }

    @Override
    public void initialize(String serviceKey, String qosServerName) {
        this.serviceKey = Preconditions.checkNotNull(serviceKey);
        this.qosServerName = Preconditions.checkNotNull(qosServerName);
    }

    @Override
    public void incident(String description, String details) throws NotifyEventException {
        PagerDutyEventsClient pagerDutyEventsClient = PagerDutyEventsClient.create();
        TriggerIncident incident = TriggerIncident.TriggerIncidentBuilder
                .create(serviceKey, description)
                .client(qosServerName)
                .details(details)
                .build();
        pagerDutyEventsClient.trigger(incident);
    }
}
