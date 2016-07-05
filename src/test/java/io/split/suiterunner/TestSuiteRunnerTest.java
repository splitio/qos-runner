package io.split.suiterunner;

import com.google.inject.Injector;
import io.split.methodrunner.TestMethodRunnerTest;
import io.split.testrunner.junit.TestResult;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class TestSuiteRunnerTest {

    @Test
    public void runValidSuite() throws IOException, InterruptedException {
        String[] arguments = {
                "-suites",
                "SUITE1,suite2",
                "-suitesPackage",
                "io.split",
                "-parallel",
                "2",
                "-conf",
                TestMethodRunnerTest.PROPERTIES
        };
        Injector injector = TestSuiteRunner.createInjector(arguments);
        TestSuiteRunner runner = injector.getInstance(TestSuiteRunner.class);
        List<TestResult> results = runner.call();
        Assert.assertEquals(6, results.size());
        Assert.assertTrue(results.get(0).getResult().wasSuccessful());
        Assert.assertTrue(results.get(1).getResult().wasSuccessful());
        Assert.assertTrue(results.get(2).getResult().wasSuccessful());
        Assert.assertTrue(results.get(3).getResult().wasSuccessful());
        Assert.assertTrue(results.get(4).getResult().wasSuccessful());
        Assert.assertTrue(results.get(5).getResult().wasSuccessful());
    }
}
