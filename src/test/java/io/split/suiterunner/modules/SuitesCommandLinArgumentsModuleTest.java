package io.split.suiterunner.modules;

import com.beust.jcommander.ParameterException;
import io.split.testrunner.util.Suites;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

@Suites("SUITE2")
public class SuitesCommandLinArgumentsModuleTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

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
        expectedException.expectMessage("does not exist");
        String[] arguments = {
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package",
                "-confs",
                "inexistent"
        };
        SuiteCommandLineArgumentsModule argumentsModule = new SuiteCommandLineArgumentsModule(arguments);
        argumentsModule.propertiesPath();
    }

    @Test
    public void createWithExistentNoConf() throws IOException {
        File conf = temporaryFolder.newFile("conf");
        String[] arguments = {
                "-suites",
                "a,b,c",
                "-suitesPackage",
                "package",
                "-confs",
                conf.getAbsolutePath()
        };
        SuiteCommandLineArgumentsModule argumentsModule = new SuiteCommandLineArgumentsModule(arguments);
        Assert.assertEquals(conf.getAbsolutePath(), argumentsModule.propertiesPath().get(0).toString());
    }

}
