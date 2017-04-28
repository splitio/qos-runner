package io.split.qos.server.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.split.qos.dtos.StateDTO;
import io.split.qos.dtos.Status;
import io.split.qos.server.QOSServerState;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/state")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class StateResource {
    private final QOSServerState state;

    @Inject
    public StateResource(QOSServerState state) {
        this.state = Preconditions.checkNotNull(state);
    }

    @GET
    public Response state() {
        if (state.status().equals(Status.ACTIVE)) {
            return Response.ok(StateDTO.active(state.who(), state.activeSince(), state.lastTestFinished())).build();
        } else {
            return Response.ok(StateDTO.paused(state.who(), state.pausedSince(), state.lastTestFinished())).build();
        }
    }
}
