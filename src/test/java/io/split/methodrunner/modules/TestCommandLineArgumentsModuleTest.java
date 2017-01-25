package io.split.methodrunner.modules;

import com.beust.jcommander.ParameterException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class TestCommandLineArgumentsModuleTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void createWithNoConf() {
        expectedException.expect(ParameterException.class);
        expectedException.expectMessage("-conf");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.SuiteForTestMethodRunner",
                "-test",
                "firstTest",
        };
        TestCommandLineArgumentsModule module = new TestCommandLineArgumentsModule(arguments);
        module.propertiesPath();
    }

    @Test
    public void createWithInexistentNoConf() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("does not exist");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.SuiteForTestMethodRunner",
                "-test",
                "firstTest",
                "-confs",
                "inexistent"
        };
        TestCommandLineArgumentsModule argumentsModule = new TestCommandLineArgumentsModule(arguments);
        argumentsModule.propertiesPath();
    }

    @Test
    public void createWithExistentNoConf() throws IOException {
        File conf = temporaryFolder.newFile("conf");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.SuiteForTestMethodRunner",
                "-test",
                "firstTest",
                "-confs",
                conf.getAbsolutePath()
        };
        TestCommandLineArgumentsModule argumentsModule = new TestCommandLineArgumentsModule(arguments);
        Assert.assertEquals(conf.getAbsolutePath(), argumentsModule.propertiesPath().get(0).toString());
        Assert.assertEquals(1, argumentsModule.propertiesPath().size());
    }

}
