package io.split.qos.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
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

    public Slack slack;

    @JsonProperty
    public Slack getSlack() {
        if (slack == null) {
            throw new IllegalStateException("Please configure slack on the yaml");
        }
        return slack;
    }

    public static class Slack {
        @NotEmpty
        private String botToken;

        @JsonProperty
        public String getBotToken() {
            if (Strings.isNullOrEmpty(this.botToken)) {
                throw new IllegalStateException("Please configure botToken on the yaml");
            }
            return this.botToken;
        }

        @NotEmpty
        private String verboseChannel;

        @JsonProperty
        public String getVerboseChannel() {
            if (Strings.isNullOrEmpty(this.verboseChannel)) {
                throw new IllegalStateException("Please configure verboseChannel on the yaml");
            }
            return this.verboseChannel;
        }

        @NotEmpty
        private String digestChannel;

        @JsonProperty
        public String getDigestChannel() {
            if (Strings.isNullOrEmpty(this.digestChannel)) {
                throw new IllegalStateException("Please configure digestChannel on the yaml");
            }
            return this.digestChannel;
        }
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


    //Comma separated list of configs.
    @JsonProperty
    public String getConfig() {
        return config;
    }
}
