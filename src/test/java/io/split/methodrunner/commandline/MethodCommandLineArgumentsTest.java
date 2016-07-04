package io.split.methodrunner.commandline;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MethodCommandLineArgumentsTest {

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
    public void initalizeWithValidClassAndTest() {
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest"
        };
        MethodCommandLineArguments theArgs = MethodCommandLineArguments.initialize(arguments);
        Assert.assertEquals("initalizeWithValidClassAndTest", theArgs.test().getName());
        Assert.assertEquals(1, theArgs.quantity());
        Assert.assertEquals(1, theArgs.parallel());
        Assert.assertEquals("", theArgs.conf());
    }

    @Test
    public void initalizeWithQuantityParallelAndConf() {
        String[] arguments = {
                "-clazz",
                "io.split.methodrunner.commandline.MethodCommandLineArgumentsTest",
                "-test",
                "initalizeWithValidClassAndTest",
                "-parallel",
                "3",
                "-quantity",
                "5",
                "-conf",
                "conf/the.properties"
        };
        MethodCommandLineArguments theArgs = MethodCommandLineArguments.initialize(arguments);
        Assert.assertEquals("initalizeWithValidClassAndTest", theArgs.test().getName());
        Assert.assertEquals(5, theArgs.quantity());
        Assert.assertEquals(3, theArgs.parallel());
        Assert.assertEquals("conf/the.properties", theArgs.conf());
    }
}
