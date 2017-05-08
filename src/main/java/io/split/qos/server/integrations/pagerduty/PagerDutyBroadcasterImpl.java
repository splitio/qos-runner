package io.split.qos.server.integrations.pagerduty;

import com.github.dikhan.PagerDutyEventsClient;
import com.github.dikhan.domain.EventResult;
import com.github.dikhan.domain.ResolveIncident;
import com.github.dikhan.domain.TriggerIncident;
import com.github.dikhan.exceptions.NotifyEventException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.util.TestId;

import java.util.Map;

@Singleton
public class PagerDutyBroadcasterImpl implements PagerDutyBroadcaster {
    private String serviceKey;
    private String qosServerName;
    private final Map<TestId, String> incidents;

    @Inject
    public PagerDutyBroadcasterImpl() {
        this.incidents = Maps.newConcurrentMap();
    }

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
    public void incident(TestId testId, String details) throws NotifyEventException {
        Preconditions.checkNotNull(testId);

        PagerDutyEventsClient pagerDutyEventsClient = PagerDutyEventsClient.create();
        TriggerIncident incident = TriggerIncident.TriggerIncidentBuilder
                .create(serviceKey, testId.toString())
                .client(qosServerName)
                .details(details)
                .build();
        EventResult trigger = pagerDutyEventsClient.trigger(incident);
        this.incidents.put(testId, trigger.getIncidentKey());
    }

    @Override
    public void resolve(TestId testId) throws NotifyEventException {
        String incidentKey = incidents.get(testId);
        if (!Strings.isNullOrEmpty(incidentKey)) {
            incidents.remove(testId);
            ResolveIncident resolve = ResolveIncident.ResolveIncidentBuilder
                    .create(serviceKey, incidentKey)
                    .description(String.format("%s recovered", testId.toString()))
                    .details("Resolving - QOS Test Recovered")
                    .build();
            PagerDutyEventsClient pagerDutyEventsClient = PagerDutyEventsClient.create();
            pagerDutyEventsClient.resolve(resolve);
        }
    }
}
