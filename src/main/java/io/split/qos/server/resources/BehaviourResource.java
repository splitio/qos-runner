package io.split.qos.server.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.split.qos.server.QOSServerBehaviour;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Optional;

@Path("/behaviour")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class BehaviourResource {

    private final QOSServerBehaviour behaviour;

    @Inject
    public BehaviourResource(QOSServerBehaviour behaviour) {
        this.behaviour = Preconditions.checkNotNull(behaviour);
    }

    @POST
    @Path("pause/{who}")
    public Response pause(@PathParam("who") String who) {
        Preconditions.checkNotNull(who);
        behaviour.pause(who);
        return Response.ok().build();
    }

    @POST
    @Path("runall")
    public Response runAll() {
        behaviour.runAllNow();
        return Response.ok().build();
    }

    @POST
    @Path("run/{who}")
    public Response runTest(@PathParam("who") String who) {
        Preconditions.checkNotNull(who);
        var args = who.split("#");
        if (args.length == 1) {
            var testsexecuted = behaviour.runTestsNow(Optional.empty(),args[0]);
        } else {
            var testsexecuted = behaviour.runTestsNow(Optional.of(args[0]), args[1]);
        }
        return Response.ok().build();
    }

    @POST
    @Path("resume/{who}")
    public Response resume(@PathParam("who") String who) {
        Preconditions.checkNotNull(who);
        behaviour.resume(who);
        return Response.ok().build();
    }
}
