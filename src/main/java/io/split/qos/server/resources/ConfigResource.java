package io.split.qos.server.resources;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.dtos.ConfigDTO;
import io.split.qos.server.modules.QOSPropertiesModule;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Properties;

@Path("/config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Singleton
public class ConfigResource {

    private final Properties configuration;

    @Inject
    public ConfigResource(@Named(QOSPropertiesModule.CONFIGURATION) Properties configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
    }

    @GET
    public Response config() {
        return Response.ok(ConfigDTO.from(configuration)).build();
    }
}
