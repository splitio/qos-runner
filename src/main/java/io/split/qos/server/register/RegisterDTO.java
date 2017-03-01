package io.split.qos.server.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class RegisterDTO {

    @JsonProperty
    public final String serverName;

    @JsonProperty
    public final String serverURL;

    public RegisterDTO(String serverName, String serverURL) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.serverURL = Preconditions.checkNotNull(serverURL);
    }

    @Override
    public String toString() {
        return String.format("{serverName: %s, serverURL: %s}", serverName, serverURL);
    }
}
