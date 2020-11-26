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
    private String teamName;

    @NotEmpty
    private String config;

    @JsonProperty
    public String getServerName() {
        return serverName;
    }

    @JsonProperty
    public String getTeamName() {
        return teamName;
    }

    public Register register;

    @JsonProperty
    public Register getRegister() {
        return register;
    }

    public static class Register {
        @NotEmpty
        private String dashboardURL;

        private String hostURL;

        @JsonProperty
        public String getDashboardURL() {
            return this.dashboardURL;
        }

        @JsonProperty
        public String getHostURL() {
            return this.hostURL;
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

    public Slack slack;

    @JsonProperty
    public Slack getSlack() {
        return slack;
    }

    public static class Slack {
        @NotEmpty
        private String token;

        @JsonProperty
        public String getToken() {
            return this.token;
        }

        @NotEmpty
        private String verboseChannel;

        @JsonProperty
        public String getVerboseChannel() {
            return this.verboseChannel;
        }

        @NotEmpty
        private String digestChannel;

        @JsonProperty
        public String getDigestChannel() {
            return this.digestChannel;
        }
    }

    public Test test;

    @JsonProperty
    public Test getTest() {
        return test;
    }

    public static class Test {
        @NotEmpty
        private Integer consecutiveFailures = 2;

        @JsonProperty
        public Integer getConsecutiveFailures() {
            return this.consecutiveFailures;
        }

    }

    public Datadog datadog;

    @JsonProperty
    public Datadog getDatadog() {
        return datadog;
    }

    public static class Datadog {
        @NotEmpty
        private String host;

        @JsonProperty
        public String getHost() {
            return this.host;
        }

        @NotEmpty
        private int port;

        @JsonProperty
        public Integer getPort() {
            return this.port;
        }
    }

    //Comma separated list of configs.
    @JsonProperty
    public String getConfig() {
        return config;
    }
}
