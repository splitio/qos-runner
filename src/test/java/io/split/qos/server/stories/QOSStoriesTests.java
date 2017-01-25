package io.split.qos.server.stories;

import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.tests.QOSServerBehaviourTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

import java.util.Optional;

public class QOSStoriesTests {

    @Test
    public void getLastFailedWorks() {
        QOSStories qosStories = new QOSStories();
        Assert.assertFalse(qosStories.getLatestFailedStory().isPresent());
        Story firstStory = new Story();
        Story lastStory = new Story();
        qosStories.addStory(Description.createSuiteDescription(this.getClass()), firstStory);
        qosStories.addStory(Description.createSuiteDescription(QOSStoriesTests.class), lastStory);
        Assert.assertFalse(qosStories.getLatestFailedStory().isPresent());
        Story failedStory = new Story();
        failedStory.setSucceeded(false);
        qosStories.addStory(Description.createSuiteDescription(QOSServerBehaviourTests.class), failedStory);
        Assert.assertEquals(failedStory, qosStories.getLatestFailedStory().get());
        Story oneMore = new Story();
        qosStories.addStory(Description.createSuiteDescription(BaseCaseForTest.class), oneMore);
        Assert.assertEquals(failedStory, qosStories.getLatestFailedStory().get());
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
