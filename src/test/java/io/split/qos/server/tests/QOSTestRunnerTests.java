package io.split.qos.server.tests;

import io.split.qos.server.BaseCaseForTest;
import io.split.qos.server.testrunner.QOSTestResult;
import io.split.qos.server.testrunner.QOSTestRunner;
import io.split.qos.server.testrunner.QOSTestRunnerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;

public class QOSTestRunnerTests extends BaseCaseForTest {

    @Test
    public void testSucceed() throws Exception {
        Method emptyMethod = SmokeExampleTest.class.getMethod("testOne");

        QOSTestRunnerFactory runnerFactory = injector().getInstance(QOSTestRunnerFactory.class);
        QOSTestRunner qosTestRunner = runnerFactory.create(emptyMethod);
        QOSTestResult result = qosTestRunner.call();
        Assert.assertNotNull(result);
        Assert.assertTrue(result.getResult().wasSuccessful());
    }
}
