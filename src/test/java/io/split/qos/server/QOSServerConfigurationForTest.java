package io.split.qos.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.Singleton;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

@Singleton
public class QOSServerConfigurationForTest extends Configuration {

    @NotEmpty
    private String serverName;

    @NotEmpty
    private String config;

    @JsonProperty
    public String getServerName() {
        return serverName;
    }

    @JsonProperty
    public String getConfig() {
        return config;
    }
}
