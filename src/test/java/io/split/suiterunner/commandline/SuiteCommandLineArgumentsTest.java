package io.split.suiterunner.commandline;

import com.beust.jcommander.ParameterException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

public class SuiteCommandLineArgumentsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testNoArguments() {
        expectedException.expect(ParameterException.class);
        expectedException.expectMessage("The following options are required: [-suitesPackage], [-suites], [-confs]");
        String[] arguments = { };
        SuiteCommandLineArguments.initialize(arguments);
    }

    @Test
    public void testWithParameters() {
        String[] arguments = {
                "-confs",
                "conf",
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package"
        };
        SuiteCommandLineArguments lineArguments = SuiteCommandLineArguments.initialize(arguments);
        Assert.assertEquals("conf", lineArguments.confs().get(0));
        Assert.assertEquals(Arrays.asList("a", "b", "c"), lineArguments.suites());
        Assert.assertEquals("package", lineArguments.suitesPackage());
        Assert.assertEquals(1, lineArguments.parallel());
    }

    @Test
    public void testWithParallel() {
        String[] arguments = {
                "-confs",
                "conf",
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package",
                "-parallel",
                "3"
        };
        SuiteCommandLineArguments lineArguments = SuiteCommandLineArguments.initialize(arguments);
        Assert.assertEquals("conf", lineArguments.confs().get(0));
        Assert.assertEquals(Arrays.asList("a", "b", "c"), lineArguments.suites());
        Assert.assertEquals("package", lineArguments.suitesPackage());
        Assert.assertEquals(3, lineArguments.parallel());
    }
}
