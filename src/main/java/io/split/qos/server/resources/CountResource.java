package io.split.qos.server.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.split.qos.dtos.CountDTO;
import io.split.qos.server.QOSServerState;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/count")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class CountResource {

    private final QOSServerState state;

    @Inject
    public CountResource(QOSServerState state) {
        this.state = Preconditions.checkNotNull(state);
    }
    
    @GET
    public Response count() {
        int failed = state.failedTests().size();
        int missing = state.missingTests().size();
        int succeeded = state.succeededTests().size();
        return Response.ok(CountDTO.get(succeeded, missing, failed)).build();
    }

}
