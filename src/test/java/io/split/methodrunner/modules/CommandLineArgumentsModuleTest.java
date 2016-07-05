package io.split.methodrunner.modules;

import com.beust.jcommander.ParameterException;
import io.split.methodrunner.TestMethodRunnerTest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class CommandLineArgumentsModuleTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createWithNoConf() {
        expectedException.expect(ParameterException.class);
        expectedException.expectMessage("-conf");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest"
        };
        CommandLineArgumentsModule module = new CommandLineArgumentsModule(arguments);
        module.propertiesPath();
    }

    @Test
    public void createWithInexistentNoConf() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("does not exists");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest",
                "-conf",
                "inexistent"
        };
        CommandLineArgumentsModule argumentsModule = new CommandLineArgumentsModule(arguments);
        argumentsModule.propertiesPath();
    }

    @Test
    public void createWithExistentNoConf() {
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest",
                "-conf",
                TestMethodRunnerTest.PROPERTIES
        };
        CommandLineArgumentsModule argumentsModule = new CommandLineArgumentsModule(arguments);
        Assert.assertEquals("conf/qos.test.properties", argumentsModule.propertiesPath().toString());
    }

}
