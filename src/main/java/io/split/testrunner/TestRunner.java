package io.split.testrunner;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.split.qos.server.QOSServerApplication;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.failcondition.FailWith;
import io.split.qos.server.failcondition.SimpleFailCondition;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.modules.QOSCommonModule;
import io.split.qos.server.modules.QOSFailWithModule;
import io.split.testrunner.guice.ExtraModules;
import io.split.testrunner.guice.GuiceModules;
import io.split.testrunner.util.GuiceInitializator;
import io.split.testrunner.util.PropertiesConfig;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * The base class should use @RunWith with this Runners
 */
public class TestRunner extends BlockJUnit4ClassRunner {

    private final transient Injector injector;

    /**
     * Constructor.
     *
     * @param clazz The in test.
     * @throws InitializationError If something goes wrong.
     */
    public TestRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);

        // This is for running tests from the IDE.
        // Basically you can set the annotation @QOSConfig and that will be used as properties file.
        // If it is not already set, meaning the server didn't set it.
        if (GuiceInitializator.getPaths().isEmpty() && clazz.isAnnotationPresent(PropertiesConfig.class)) {
            PropertiesConfig annotation = clazz.getAnnotation(PropertiesConfig.class);
            Arrays.asList(annotation.value())
                    .stream()
                    .forEach(path -> GuiceInitializator.addPath(Paths.get(path)));

        }

        this.injector = this.createInjectorFor(clazz);
    }

    @Override
    public final Object createTest() throws Exception {
        final Object obj = super.createTest();
        this.injector.injectMembers(obj);
        return obj;
    }

    /**
     * Create a Guice Injector for the class under test.
     *
     * @return A Guice Injector instance.
     * @throws InitializationError If couldn't instantiate a module.
     */
    private Injector createInjectorFor(Class<?> theClass) throws InitializationError {
        List<AbstractModule> modules = Lists.newArrayList();

        for(Class<? extends AbstractModule> clazz : getGuiceModulesFor(theClass)) {
            try {
                modules.add(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
        for(Class<? extends AbstractModule> clazz : getExtraModulesFor(theClass)) {
            try {
                modules.add(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        if (GuiceInitializator.isQos()) {
            if (QOSServerApplication.injector != null) {
                modules.add(new QOSCommonModule(
                        QOSServerApplication.injector.getInstance(SlackCommon.class),
                        QOSServerApplication.injector.getInstance(QOSServerState.class)));
            }
        }
        modules.add(getFailCondition(theClass));
        return Guice.createInjector(modules);
    }

    private List<Class<? extends AbstractModule>> getGuiceModulesFor(final Class<?> clazz) throws InitializationError {
        final GuiceModules annotation = clazz.getAnnotation(GuiceModules.class);

        if (annotation == null) {
            final String message = String.format("Missing @GuiceModules annotation for unit test '%s'", clazz.getName());
            throw new InitializationError(message);
        }

        return Lists.newArrayList(annotation.value());
    }

    private List<Class<? extends AbstractModule>> getExtraModulesFor(final Class<?> clazz) throws InitializationError {
        ExtraModules annotation = clazz.getAnnotation(ExtraModules.class);
        return annotation == null ? Lists.newArrayList() : Lists.newArrayList(annotation.value());
    }

    private AbstractModule getFailCondition(Class<?> clazz) throws InitializationError {
        FailWith failWith = clazz.getAnnotation(FailWith.class);
        if (failWith == null) {
            return new QOSFailWithModule(SimpleFailCondition.class);
        }

        return new QOSFailWithModule(failWith.value());
    }
}