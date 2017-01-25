package io.split.qos.server.stories;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.List;

public class Story {

    private final List<Step> steps;
    private long started;
    private long finished;
    private String title;
    private String description;

    public Story() {
        this.steps = Lists.newArrayList();
    }

    public String title() {
        return title();
    }

    public String description() {
        return null;
    }

    public List<Step> steps() {
        return steps();
    }

    public Long started() {
        return started;
    }

    public Long finished() {
        return finished;
    }

    public void addStep(Step step) {
        steps.add(Preconditions.checkNotNull(step));
    }

    public void start(long started) {
        this.started = started;
    }

    public void finish(long finished) {
        this.finished = finished;
    }

    public void title(String title) {
        this.title = Preconditions.checkNotNull(title);
    }

    public void description(String description) {
        this.description = Preconditions.checkNotNull(description);
    }

}
