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
    @Description("Short description of the test")
    public void testOne() {
        System.out.print("FINISHED ONE");
        throw new IllegalStateException("Server returned HTTP response code: 502 for URL");
    }

    @Test
    @Title("Second Test")
    @Description("Short description of the test")
    public void testTwo() {
        System.out.print("FINISHED SECOND");
        throw new IllegalStateException("HTTP 503 Service Unavailable: Back-end server is at capacity");
    }
}
