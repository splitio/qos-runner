package io.split.suiterunner;

import com.google.inject.Injector;
import io.split.testrunner.junit.TestResult;
import io.split.testrunner.util.GuiceInitializator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestSuiteRunnerTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void runValidSuite() throws IOException, InterruptedException {
        GuiceInitializator.initialize();
        File conf = temporaryFolder.newFile();
        String[] arguments = {
                "-suites",
                "SUITE_FOR_TEST_SUITE,suite2",
                "-suitesPackage",
                "io.split",
                "-parallel",
                "2",
                "-confs",
                conf.getAbsolutePath()
        };
        Injector injector = TestSuiteRunner.createInjector(arguments);
        TestSuiteRunner runner = injector.getInstance(TestSuiteRunner.class);
        List<TestResult> results = runner.call();
        Assert.assertEquals(1, results.size());
        Assert.assertTrue(results.get(0).getResult().wasSuccessful());
    }
}
