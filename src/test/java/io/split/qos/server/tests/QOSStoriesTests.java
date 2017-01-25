package io.split.qos.server.tests;

import io.split.qos.server.stories.QOSStories;
import io.split.qos.server.stories.Story;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.Optional;

public class QOSStoriesTests {

    @Test
    public void getLastWorks() {
        QOSStories qosStories = new QOSStories();
        Assert.assertTrue(!qosStories.getLatestStory().isPresent());
        Story firstStory = new Story();
        Story lastStory = new Story();
        qosStories.addStory(Description.createSuiteDescription(this.getClass()), firstStory);
        qosStories.addStory(Description.createSuiteDescription(QOSStoriesTests.class), lastStory);
        Assert.assertEquals(lastStory, qosStories.getLatestStory().get());
    }

    @Test
    public void getStoryWorks() {
        QOSStories qosStories = new QOSStories();
        Story firstStory = new Story();
        qosStories.addStory(Description.createTestDescription(this.getClass(), "getLastWorks"), firstStory);
        Assert.assertTrue(!qosStories.getStory(Optional.empty(), "notexistent").isPresent());
        Assert.assertTrue(!qosStories.getStory(Optional.of("QOSStoriesTests"), "nope").isPresent());
        Assert.assertTrue(!qosStories.getStory(Optional.of("nope"), "getLastWorks").isPresent());
        Assert.assertEquals(firstStory, qosStories.getStory(Optional.of("QOSStoriesTests"), "getLastWorks").get());
        Assert.assertEquals(firstStory, qosStories.getStory(Optional.empty(), "getLastWorks").get());
    }
}
