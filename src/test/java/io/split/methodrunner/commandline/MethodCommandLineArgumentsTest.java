package io.split.methodrunner.commandline;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

public class MethodCommandLineArgumentsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void initializeWithInexistentClass() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("should be a class");
        String[] arguments = {
                "-clazz",
                "nonexistent",
                "-test",
                ""
        };
        MethodCommandLineArguments.initialize(arguments);
    }

    @Test
    public void initializeWithInexistentTest() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("should be a method");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "inexistent"
        };
        MethodCommandLineArguments.initialize(arguments);
    }

    @Test
    public void initalizeWithValidClassAndTest() throws IOException {
        File conf = temporaryFolder.newFile("conf");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest",
                "-confs",
                conf.getAbsolutePath()
        };
        MethodCommandLineArguments theArgs = MethodCommandLineArguments.initialize(arguments);
        Assert.assertEquals("initalizeWithValidClassAndTest", theArgs.test().getName());
        Assert.assertEquals(1, theArgs.quantity());
        Assert.assertEquals(1, theArgs.parallel());
        Assert.assertEquals(Lists.newArrayList(conf.getAbsolutePath()), theArgs.confs());
    }

    @Test
    public void initalizeWithQuantityParallelAndConf() throws IOException {
        File conf = temporaryFolder.newFile("conf");
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest",
                "-parallel",
                "3",
                "-quantity",
                "5",
                "-confs",
                conf.getAbsolutePath()
        };
        MethodCommandLineArguments theArgs = MethodCommandLineArguments.initialize(arguments);
        Assert.assertEquals("initalizeWithValidClassAndTest", theArgs.test().getName());
        Assert.assertEquals(5, theArgs.quantity());
        Assert.assertEquals(3, theArgs.parallel());
        Assert.assertEquals(Lists.newArrayList(conf.getAbsolutePath()), theArgs.confs());
    }
}
