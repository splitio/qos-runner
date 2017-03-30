package io.split.qos.server.tests;

import io.split.qos.server.util.TestId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;

public class TestIdTests {

    @Test
    public void testContains() {
        Description testDescription = Description.createTestDescription(TestIdTests.class, "TestName");
        TestId testId = TestId.fromDescription(testDescription);
        Assert.assertTrue(testId.contains("testname"));
        Assert.assertTrue(testId.contains("testidtests"));
        Assert.assertTrue(testId.contains("TestName"));
        Assert.assertTrue(testId.contains("TestIdTests"));
        Assert.assertTrue(testId.contains("Test"));
        Assert.assertTrue(testId.contains("TestId"));
        Assert.assertTrue(testId.contains("Name"));
        Assert.assertTrue(testId.contains("test"));
        Assert.assertTrue(testId.contains("testid"));
        Assert.assertTrue(testId.contains("name"));
        Assert.assertFalse(testId.contains("what"));
        Assert.assertFalse(testId.contains("testnames"));
        Assert.assertFalse(testId.contains("TestIdTEstss"));
        Assert.assertTrue(testId.contains("TestId", "TestName"));
        Assert.assertTrue(testId.contains("Test", "Name"));
        Assert.assertFalse(testId.contains("Test", "non"));
        Assert.assertFalse(testId.contains("non", "Name"));
    }
}
