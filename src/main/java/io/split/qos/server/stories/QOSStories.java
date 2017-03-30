package io.split.qos.server.stories;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.util.TestId;
import org.junit.runner.Description;

import java.util.Map;
import java.util.Optional;

@Singleton
public class QOSStories {

    private final Map<TestId, Story> stories;
    private Optional<Story> latestFailed;

    @Inject
    public QOSStories() {
        this.stories = Maps.newConcurrentMap();
        this.latestFailed = Optional.empty();
    }

    public Optional<Story> getStory(Optional<String> fuzzyClass, String fuzzyMethod) {
        Optional<Map.Entry<TestId, Story>> any = stories
                .entrySet()
                .stream()
                .filter(entry -> {
                    if (entry == null
                            || entry.getKey() == null
                            || entry.getValue() == null) {
                        return false;
                    }
                    TestId testId = entry.getKey();
                    if (fuzzyClass.isPresent()) {
                        return testId.contains(fuzzyClass.get(), fuzzyMethod);
                    } else {
                        return testId.contains(fuzzyMethod);
                    }
                })
                .findAny();
        if (any.isPresent()) {
            return Optional.of(any.get().getValue());
        }
        return Optional.empty();
    }

    public Optional<Story> getLatestFailedStory() {
        return latestFailed;
    }

    public void addStory(Description description, Story story) {
        Preconditions.checkNotNull(description);
        Preconditions.checkNotNull(story);
        stories.put(TestId.fromDescription(description), story);
        if (!story.isSucceeded()) {
            latestFailed = Optional.of(story);
        }
    }
}
