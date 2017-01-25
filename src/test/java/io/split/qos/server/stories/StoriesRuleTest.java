package io.split.qos.server.stories;

import io.split.qos.server.stories.annotations.Title;
import io.split.testrunner.util.Suites;
import io.split.testrunner.util.TestsFinder;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

@Suites("SUITE_FOR_RULE")
public class StoriesRuleTest {

    @Test
    public void testNoTitleNoDescription() throws Exception {
        Story story = new Story();
        StoriesRule rule = new StoriesRule(new QOSStories(),
                "SUITE_FOR_RULE",
                this.getClass().getPackage().getName(),
                new TestsFinder(),
                story);

        rule.setStoryTitleAndDescription(Description.createTestDescription(this.getClass(), "testNoTitleNoDescription"));
        Assert.assertFalse(story.title().isPresent());
        Assert.assertFalse(story.description().isPresent());
    }

    @Title("THE TITLE")
    @Test
    public void testTitleNoDescription() throws Exception {
        Story story = new Story();
        StoriesRule rule = new StoriesRule(new QOSStories(),
                "SUITE_FOR_RULE",
                this.getClass().getPackage().getName(),
                new TestsFinder(),
                story);

        rule.setStoryTitleAndDescription(Description.createTestDescription(this.getClass(), "testTitleNoDescription"));
        Assert.assertEquals("THE TITLE", story.title().get());
        Assert.assertFalse(story.description().isPresent());
    }

    @io.split.qos.server.stories.annotations.Description("THE DESCRIPTION")
    @Test
    public void testNoTitleDescription() throws Exception {
        Story story = new Story();
        StoriesRule rule = new StoriesRule(new QOSStories(),
                "SUITE_FOR_RULE",
                this.getClass().getPackage().getName(),
                new TestsFinder(),
                story);

        rule.setStoryTitleAndDescription(Description.createTestDescription(this.getClass(), "testNoTitleDescription"));
        Assert.assertEquals("THE DESCRIPTION", story.description().get());
        Assert.assertFalse(story.title().isPresent());
    }

    @io.split.qos.server.stories.annotations.Description("THE DESCRIPTION")
    @Title("THE TITLE")
    @Test
    public void testTitleDescription() throws Exception {
        Story story = new Story();
        StoriesRule rule = new StoriesRule(new QOSStories(),
                "SUITE_FOR_RULE",
                this.getClass().getPackage().getName(),
                new TestsFinder(),
                story);

        rule.setStoryTitleAndDescription(Description.createTestDescription(this.getClass(), "testTitleDescription"));
        Assert.assertEquals("THE DESCRIPTION", story.description().get());
        Assert.assertEquals("THE TITLE", story.title().get());
    }
}
