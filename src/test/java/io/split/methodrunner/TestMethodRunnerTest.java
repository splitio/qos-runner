package io.split.methodrunner;

import com.google.inject.Injector;
import io.split.testrunner.junit.TestResult;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestMethodRunnerTest {
    public static final String PROPERTIES = "conf/qos.test.properties";

    @Test
    public void runValidTest() throws InterruptedException {
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest",
                "-parallel",
                "2",
                "-quantity",
                "2",
                "-conf",
                PROPERTIES
        };
        Injector injector = TestMethodRunner.createInjector(arguments);
        TestMethodRunner runner = injector.getInstance(TestMethodRunner.class);
        List<TestResult> results = runner.call();
        Assert.assertEquals(2, results.size());
        Assert.assertTrue(results.get(0).getResult().wasSuccessful());
        Assert.assertTrue(results.get(1).getResult().wasSuccessful());
    }
}
