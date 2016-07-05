package io.split.suiterunner.modules;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.split.methodrunner.TestMethodRunnerTest;
import io.split.methodrunner.modules.TestRunnerPropertiesModule;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.testrunner.util.GuiceInitializator;
import io.split.testrunner.util.Suites;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.nio.file.Paths;
import java.util.List;

@Suites("SUITE1")
public class SuiteRunnerPropertiesModuleTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        GuiceInitializator.setPath(Paths.get(""));
    }

    @Test
    public void testWithEmpty() {
        expectedException.expect(RuntimeException.class);
        List<Module> modules = Lists.newArrayList(new SuiteRunnerPropertiesModule());
        Guice.createInjector(modules);
    }

    @Test
    public void testWithInexistent() {
        expectedException.expect(RuntimeException.class);
        GuiceInitializator.setPath(Paths.get("inexistent"));
        List<Module> modules = Lists.newArrayList(new SuiteRunnerPropertiesModule());
        Guice.createInjector(modules);
    }

    @Test
    public void testWithExistent() {
        GuiceInitializator.setPath(Paths.get(TestMethodRunnerTest.PROPERTIES));
        List<Module> modules = Lists.newArrayList(new SuiteRunnerPropertiesModule());
        Injector injector = Guice.createInjector(modules);
        Assert.assertEquals("21",
                injector.getInstance(Key.get(String.class, Names.named(TestRunnerPropertiesModule.TIMEOUT_IN_MINUTES))));
        Assert.assertEquals("300",
                injector.getInstance(Key.get(String.class, Names.named(QOSPropertiesModule.DELAY_BETWEEN_IN_SECONDS))));
    }

}
