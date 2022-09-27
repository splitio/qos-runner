package io.split.qos.server.util;

import io.split.qos.server.stories.annotations.Description;
import io.split.qos.server.stories.annotations.Title;
import io.split.qos.server.testcase.QOSTestCase;
import io.split.testrunner.util.Suites;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;

/**
 * THIS IS USED BY UNIT TESTS.
 */
@Suites("PARALLEL_TEST")
public class ParallelExampleTest extends QOSTestCase {

    int timeout = 30000;

    @Test
    @Title("Test A")
    @Description("desc")
    public void testA() throws InterruptedException {
        Thread.sleep(timeout);
        Assert.assertTrue(true);
    }

    @Test
    @Title("Test B")
    @Description("desc")
    public void testB() throws InterruptedException {
        Thread.sleep(timeout);
        Assert.assertTrue(true);
    }

    @Test
    @Title("Test C")
    @Description("desc")
    public void testC() throws InterruptedException {
        Thread.sleep(timeout);
        Assert.assertTrue(true);
    }

    @Test
    @Title("Test D")
    @Description("desc")
    public void testD() throws InterruptedException {
        Thread.sleep(timeout);
        Assert.assertTrue(true);
    }

    @Test
    @Title("Test E")
    @Description("desc")
    public void testE() throws InterruptedException {
        Thread.sleep(timeout);
        Assert.assertTrue(true);
    }

    @Test
    @Title("Test F")
    @Description("desc")
    public void testF() throws InterruptedException {
        Thread.sleep(timeout);
        Assert.assertTrue(true);
    }
}
