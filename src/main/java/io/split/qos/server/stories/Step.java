package io.split.qos.server.stories;

import com.google.common.base.Preconditions;

public class Step {

    private final long started;
    private final String description;

    public Step(String description, long started) {
        this.started = started;
        this.description = Preconditions.checkNotNull(description);
    }

    public Long started() {
        return started;
    }

    public String description() {
        return description;
    }
}
