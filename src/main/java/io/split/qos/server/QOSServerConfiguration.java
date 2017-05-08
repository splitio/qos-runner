package io.split.qos.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * DropWizard configuration.
 */
public class QOSServerConfiguration extends Configuration {

    @NotEmpty
    private String serverName;

    @NotEmpty
    private String config;

    @JsonProperty
    public String getServerName() {
        return serverName;
    }

    public Register register;

    @JsonProperty
    public Register getRegister() {
        return register;
    }

    public static class Register {
        @NotEmpty
        private String dashboardURL;

        @JsonProperty
        public String getDashboardURL() {
            return this.dashboardURL;
        }
    }

    public PagerDuty pagerDuty;

    @JsonProperty
    public PagerDuty getPagerDuty() {
        return pagerDuty;
    }

    public static class PagerDuty {
        @NotEmpty
        private String serviceKey;

        @JsonProperty
        public String getServiceKey() {
            return this.serviceKey;

        }
    }


    //Comma separated list of configs.
    @JsonProperty
    public String getConfig() {
        return config;
    }
}
