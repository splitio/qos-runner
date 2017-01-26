package io.split.qos.server.stories;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

public class Step {

    private final long started;
    private final List<String> descriptions;
    private final String title;

    Step(long started, String title, List<String> descriptions) {
        this.started = started;
        this.title = Preconditions.checkNotNull(title);
        this.descriptions = Preconditions.checkNotNull(descriptions);
    }

    Step(long started, String title) {
        this.started = started;
        this.title = Preconditions.checkNotNull(title);
        this.descriptions = Lists.newArrayList();
    }

    Step(long started, String title, String... descriptions) {
        this.started = started;
        this.title = Preconditions.checkNotNull(title);
        this.descriptions = Arrays.asList(Preconditions.checkNotNull(descriptions));
    }

    public Long started() {
        return started;
    }

    public String title() {
        return title;
    }

    public List<String> descriptions() {
        return descriptions;
    }
}
