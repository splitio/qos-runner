package io.split.qos.server.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import io.split.qos.server.QOSTestsTracker;
import io.split.qos.server.util.TestId;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;

@Path("/running")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class RunningResource {

    private final QOSTestsTracker tracker;

    @Inject
    public RunningResource(QOSTestsTracker tracker) {
        this.tracker = Preconditions.checkNotNull(tracker);
    }

    @GET
    public Response running() throws Exception {
        List<String> testsRunning = Lists.newArrayList();

        Map<TestId, QOSTestsTracker.Tracked> tests = tracker.tests();
        for(QOSTestsTracker.Tracked track : tests.values()) {
            if (track.runner().isRunning()) {
                testsRunning.add(track.method().getName());
            }
        }
        return Response.ok(testsRunning).build();
    }
}
