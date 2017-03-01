package io.split.qos.server.register;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class RegisterDTO {

    @JsonProperty
    public final String serverName;

    @JsonProperty
    public final String serverURL;

    @JsonProperty
    private final String botToken;

    public RegisterDTO(String serverName, String serverURL, String botToken) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.serverURL = Preconditions.checkNotNull(serverURL);
        this.botToken = Preconditions.checkNotNull(botToken);
    }

    @Override
    public String toString() {
        return String.format("{serverName: %s, serverURL: %s, botToken: %s}", serverName, serverURL, botToken);
    }
}
