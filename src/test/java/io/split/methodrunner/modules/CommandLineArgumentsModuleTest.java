package io.split.methodrunner.modules;

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
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest"
        };
        CommandLineArgumentsModule argumentsModule = new CommandLineArgumentsModule(arguments);
        Assert.assertTrue(!argumentsModule.propertiesPath().isPresent());
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
        new CommandLineArgumentsModule(arguments);
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
        Assert.assertTrue(argumentsModule.propertiesPath().isPresent());
        Assert.assertEquals("conf/qos.test.properties", argumentsModule.propertiesPath().get().toString());
    }

}
