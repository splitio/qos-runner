package io.split.qos.server.stories;

import com.google.common.base.Preconditions;

import java.util.Optional;

public class Step {

    private final long started;
    private final Optional<String> description;
    private final String title;

    Step(String title, String description, long started) {
        this.started = started;
        this.title = Preconditions.checkNotNull(title);
        this.description = Optional.of(Preconditions.checkNotNull(description));
    }

    Step(String title, long started) {
        this.started = started;
        this.title = Preconditions.checkNotNull(title);
        this.description = Optional.empty();
    }

    public Long started() {
        return started;
    }

    public String title() {
        return title;
    }

    public Optional<String> description() {
        return description;
    }
}
