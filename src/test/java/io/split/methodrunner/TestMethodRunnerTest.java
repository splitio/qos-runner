package io.split.methodrunner;

import com.google.inject.Injector;
import io.split.testrunner.junit.TestResult;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestMethodRunnerTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void runValidTest() throws InterruptedException, IOException {
        File conf = temporaryFolder.newFile("conf");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.SuiteForTestMethodRunner",
                "-test",
                "firstTest",
                "-parallel",
                "2",
                "-quantity",
                "2",
                "-confs",
                conf.getAbsolutePath()
        };
        Injector injector = TestMethodRunner.createInjector(arguments);
        TestMethodRunner runner = injector.getInstance(TestMethodRunner.class);
        List<TestResult> results = runner.call();
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.get(0).getResult().wasSuccessful());
        Assert.assertTrue(results.get(1).getResult().wasSuccessful());
    }
}
