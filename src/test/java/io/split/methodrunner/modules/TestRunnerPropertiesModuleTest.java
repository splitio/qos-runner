package io.split.methodrunner.modules;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.split.qos.server.modules.QOSPropertiesModule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

public class TestRunnerPropertiesModuleTest {
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testWithEmpty() {
        List<Module> modules = Lists.newArrayList(new TestRunnerPropertiesModule(Optional.empty()));
        Injector injector = Guice.createInjector(modules);
        Assert.assertEquals("20",
                injector.getInstance(Key.get(String.class, Names.named(TestRunnerPropertiesModule.TIMEOUT_IN_MINUTES))));
    }

    @Test
    public void testWithInexistent() {
        expectedException.expect(RuntimeException.class);
        List<Module> modules = Lists.newArrayList(new TestRunnerPropertiesModule(Optional.of(Paths.get("iniexitent"))));
        Guice.createInjector(modules);
    }

    @Test
    public void testWithExistent() {
        List<Module> modules = Lists.newArrayList(new TestRunnerPropertiesModule(Optional.of(Paths.get("conf/qos.test.properties"))));
        Injector injector = Guice.createInjector(modules);
        Assert.assertEquals("21",
                injector.getInstance(Key.get(String.class, Names.named(TestRunnerPropertiesModule.TIMEOUT_IN_MINUTES))));
        Assert.assertEquals("300",
                injector.getInstance(Key.get(String.class, Names.named(QOSPropertiesModule.DELAY_BETWEEN_IN_SECONDS))));
    }

}
