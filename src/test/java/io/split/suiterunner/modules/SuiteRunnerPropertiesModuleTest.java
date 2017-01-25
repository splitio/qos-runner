package io.split.suiterunner.modules;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.split.methodrunner.modules.TestRunnerPropertiesModule;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.testrunner.util.GuiceInitializator;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SuiteRunnerPropertiesModuleTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testWithEmpty() {
        GuiceInitializator.initialize();
        expectedException.expect(RuntimeException.class);
        GuiceInitializator.addPath(Paths.get(""));
        List<Module> modules = Lists.newArrayList(new SuiteRunnerPropertiesModule());
        Guice.createInjector(modules);
    }

    @Test
    public void testWithInexistent() {
        GuiceInitializator.initialize();
        expectedException.expect(RuntimeException.class);
        GuiceInitializator.addPath(Paths.get("inexistent"));
        List<Module> modules = Lists.newArrayList(new SuiteRunnerPropertiesModule());
        Guice.createInjector(modules);
    }

    @Test
    public void testWithExistent() throws IOException {
        GuiceInitializator.initialize();
        File conf = temporaryFolder.newFile("conf2");
        BufferedWriter output = new BufferedWriter(new FileWriter(conf.getAbsolutePath(), true));
        output.write("TIMEOUT_IN_MINUTES=21");
        output.newLine();
        output.write("DELAY_BETWEEN_IN_SECONDS=300");
        output.newLine();
        output.close();

        GuiceInitializator.addPath(Paths.get(conf.getAbsolutePath()));
        List<Module> modules = Lists.newArrayList(new SuiteRunnerPropertiesModule());
        Injector injector = Guice.createInjector(modules);
        Assert.assertEquals("21",
                injector.getInstance(Key.get(String.class, Names.named(TestRunnerPropertiesModule.TIMEOUT_IN_MINUTES))));
        Assert.assertEquals("300",
                injector.getInstance(Key.get(String.class, Names.named(QOSPropertiesModule.DELAY_BETWEEN_IN_SECONDS))));
    }

}
