package io.split.qos.server.stories;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class StoriesRule implements TestRule {

    private final QOSStories stories;
    private final Story story;

    @Inject
    public StoriesRule (QOSStories stories) {
        this.stories = Preconditions.checkNotNull(stories);
        this.story = new Story();
    }

    public void addTitle() {

    }

    public void addDescription() {

    }

    public void addStep() {

    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    story.start(System.currentTimeMillis());
                    statement.evaluate();
                } finally {
                    stories.addStory(description, story);
                }
            }
        };
    }
}
