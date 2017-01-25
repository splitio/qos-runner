package io.split.qos.server.stories;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.testrunner.util.Util;
import org.junit.runner.Description;

import java.util.Map;
import java.util.Optional;

@Singleton
public class QOSStories {

    private final Map<String, Story> stories;
    private Story latest;

    @Inject
    public QOSStories() {
        this.stories= Maps.newConcurrentMap();
        this.latest = null;
    }

    public Optional<Story> getStory(Optional<String> fuzzyClass, String fuzzyMethod) {
        Optional<Map.Entry<String, Story>> any = stories
                .entrySet()
                .stream()
                .filter(entry -> {
                    if (entry == null
                            || Strings.isNullOrEmpty(entry.getKey())
                            || entry.getValue() == null) {
                        return false;
                    }
                    String testId = entry.getKey();
                    if (!testId.contains(fuzzyMethod)) {
                        return false;
                    }
                    if (fuzzyClass.isPresent()) {
                        if (!testId.contains(fuzzyClass.get())) {
                            return false;
                        }
                    }
                    return true;
                })
                .findAny();
        if (any.isPresent()) {
            return Optional.of(any.get().getValue());
        }
        return Optional.empty();
    }

    public Optional<Story> getLatestStory() {
        if (latest == null) {
            return Optional.empty();
        }
        return Optional.of(latest);
    }

    public void addStory(Description description, Story story) {
        Preconditions.checkNotNull(description);
        Preconditions.checkNotNull(story);
        stories.put(Util.id(description), story);
        latest = story;
    }
}
