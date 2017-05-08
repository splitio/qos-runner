package io.split.qos.server.util;

import io.split.qos.server.stories.annotations.Description;
import io.split.qos.server.stories.annotations.Title;
import io.split.qos.server.testcase.QOSTestCase;
import io.split.testrunner.util.Suites;
import org.junit.Assert;
import org.junit.Test;

/**
 * THIS IS USED BY UNIT TESTS.
 */
@Suites("SMOKE_FOR_TEST")
public class SmokeExampleTest extends QOSTestCase {

    @Test
    @Title("First Test")
    @Description("First updates a split, changing the treatments, then kills and restores the split. Always checking that" +
            "the manager split() method returns accordingly")
    public void testOne() {
        Assert.fail();
        System.out.print("FINISHED ONE");
    }

    @Test
    @Title("Second Test")
    @Description("Short description of the test")
    public void testTwo() {
        System.out.print("FINISHED SECOND");
    }
}
