package io.split.qos.server.stories;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.util.TestId;

import java.util.List;
import java.util.Optional;

/**
 * There will be one story per test.
 *
 * The story will be shared across all the test, so at any point you can use guice
 * to inject the story and add a step.
 */
@Singleton
public class Story {

    private final List<Step> steps;
    private Optional<Long> started;
    private Optional<Long> finished;
    private Optional<String> title;
    private Optional<String> description;
    private Optional<TestId> testId;
    private boolean succeeded;

    @Inject
    public Story() {
        this.steps = Lists.newArrayList();
        this.succeeded = true;
        this.started = Optional.empty();
        this.finished = Optional.empty();
        this.title = Optional.empty();
        this.testId = Optional.empty();
        this.description = Optional.empty();
    }

    public Optional<String> title() {
        return title;
    }

    public Optional<String> description() {
        return description;
    }

    public List<Step> steps() {
        return steps;
    }

    public Optional<Long> started() {
        return started;
    }

    public Optional<Long> finished() {
        return finished;
    }

    public void addStep(String title, String description) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(description));
        steps.add(new Step(System.currentTimeMillis(), title, description));
    }

    public void addStep(String title) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title));
        steps.add(new Step(System.currentTimeMillis(), title));
    }

    public void addStep(String title, String... descriptions) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(title));
        steps.add(new Step(System.currentTimeMillis(), title, descriptions));
    }

    public void start(long started) {
        this.started = Optional.of(started);
    }

    public void finish(long finished) {
        this.finished = Optional.of(finished);
    }

    public void title(String title) {
        this.title = Optional.of(Preconditions.checkNotNull(title));
    }

    public void description(String description) {
        this.description = Optional.of(Preconditions.checkNotNull(description));
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public void testId(TestId testId) {
        this.testId = Optional.of(Preconditions.checkNotNull(testId));
    }

    public Optional<TestId> testId() {
        return testId;
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
