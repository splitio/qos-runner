package io.split.suiterunner.modules;

import com.beust.jcommander.ParameterException;
import io.split.methodrunner.TestMethodRunnerTest;
import io.split.testrunner.util.Suites;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@Suites("SUITE2")
public class SuitesCommandLinArgumentsModuleTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createWithNoConf() {
        expectedException.expect(ParameterException.class);
        expectedException.expectMessage("-conf");
        String[] arguments = {
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package"
        };
        SuiteCommandLineArgumentsModule module = new SuiteCommandLineArgumentsModule(arguments);
        module.propertiesPath();
    }

    @Test
    public void createWithInexistentNoConf() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("does not exists");
        String[] arguments = {
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package",
                "-conf",
                "inexistent"
        };
        SuiteCommandLineArgumentsModule argumentsModule = new SuiteCommandLineArgumentsModule(arguments);
        argumentsModule.propertiesPath();
    }

    @Test
    public void createWithExistentNoConf() {
        String[] arguments = {
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package",
                "-conf",
                TestMethodRunnerTest.PROPERTIES
        };
        SuiteCommandLineArgumentsModule argumentsModule = new SuiteCommandLineArgumentsModule(arguments);
        Assert.assertEquals("conf/qos.test.properties", argumentsModule.propertiesPath().toString());
    }

}
