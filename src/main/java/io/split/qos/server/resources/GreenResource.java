package io.split.qos.server.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.split.qos.dtos.GreenDTO;
import io.split.qos.server.QOSServerState;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/green")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class GreenResource {

    private final QOSServerState state;

    @Inject
    public GreenResource(QOSServerState state) {
        this.state = Preconditions.checkNotNull(state);
    }

    @GET
    public Response green() {
        if (state.lastGreen() != null) {
            return Response.ok(GreenDTO.green(state.lastGreen())).build();
        } else {
            if (state.failedTests().isEmpty()) {
                return Response.ok(GreenDTO.waiting()).build();
            } else {
                List<String> failedNames = state
                        .failedTests()
                        .keySet()
                        .stream()
                        .map(testId -> testId.toString())
                        .collect(Collectors.toList());
                return Response.ok(GreenDTO.failed(failedNames)).build();
            }
        }
    }
}
