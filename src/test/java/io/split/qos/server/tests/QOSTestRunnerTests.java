package io.split.qos.server.tests;

import io.split.qos.server.BaseCaseForTest;
import io.split.testrunner.TestResult;
import io.split.testrunner.TestRunner;
import io.split.testrunner.TestRunnerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class QOSTestRunnerTests extends BaseCaseForTest {

    @Test
    public void testSucceed() throws Exception {
        Method emptyMethod = SmokeExampleTest.class.getMethod("testOne");

        TestRunnerFactory runnerFactory = injector().getInstance(TestRunnerFactory.class);
        TestRunner qosTestRunner = runnerFactory.create(emptyMethod);
        TestResult result = qosTestRunner.call();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getResult().wasSuccessful());
    }
}
