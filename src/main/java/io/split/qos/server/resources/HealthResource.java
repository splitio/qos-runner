package io.split.qos.server.resources;

import com.google.inject.Singleton;
import io.split.qos.server.QOSServerState;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class HealthResource {

    private final QOSServerState state;

    @Inject
    public HealthResource(QOSServerState state) {
        this.state = state;
    }

    /**
     * @return the current state.
     */
    @GET
    public Response ok() {
        return Response.ok(state).build();
    }
}
