package io.split.qos.server.tests;

import io.split.qos.server.BaseCaseForTest;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.JUnitRunnerFactory;
import io.split.testrunner.junit.TestResult;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Optional;

public class QOSTestRunnerTests extends BaseCaseForTest {

    @Test
    public void testSucceed() throws Exception {
        Method emptyMethod = SmokeExampleTest.class.getMethod("testOne");

        JUnitRunnerFactory runnerFactory = injector().getInstance(JUnitRunnerFactory.class);
        JUnitRunner qosTestRunner = runnerFactory.create(emptyMethod, Optional.empty());
        TestResult result = qosTestRunner.call();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getResult().wasSuccessful());
    }
}
