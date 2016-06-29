package io.split.qos.server.guice;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.split.qos.server.QOSServerApplication;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.failcondition.FailWith;
import io.split.qos.server.failcondition.SimpleFailCondition;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.util.QOSConfig;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

/**
 * The base class should use @RunWith with this Runners
 */
public class QOSRunner extends BlockJUnit4ClassRunner {

    private final transient Injector injector;

    /**
     * Constructor.
     *
     * @param clazz The in test.
     * @throws InitializationError If something goes wrong.
     */
    public QOSRunner(final Class<?> clazz) throws InitializationError {
        super(clazz);

        // This is for running tests from the IDE.
        // Basically you can set the annotation @QOSConfig and that will be used as properties file.
        // If it is not already set, meaning the server didn't set it.
        if (Strings.isNullOrEmpty(QOSPropertiesFinderHack.getPath()) && clazz.isAnnotationPresent(QOSConfig.class)) {
            QOSConfig annotation = clazz.getAnnotation(QOSConfig.class);
            String configPath = annotation.value();
            QOSPropertiesFinderHack.setPath(configPath);
            QOSPropertiesFinderHack.setQos(true);
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

        if (QOSServerApplication.injector != null) {
            modules.add(new CommonModule(
                    QOSServerApplication.injector.getInstance(SlackCommon.class),
                    QOSServerApplication.injector.getInstance(QOSServerState.class)));
        }
        if (QOSPropertiesFinderHack.isQos()) {
            modules.add(getFailCondition(theClass));
        }
        return Guice.createInjector(modules);
    }

    private List<Class<? extends AbstractModule>> getGuiceModulesFor(final Class<?> clazz) throws InitializationError {
        final QOSGuiceModules annotation = clazz.getAnnotation(QOSGuiceModules.class);

        if (annotation == null) {
            final String message = String.format("Missing @QOSGuiceModules annotation for unit test '%s'", clazz.getName());
            throw new InitializationError(message);
        }

        return Lists.newArrayList(annotation.value());
    }

    private List<Class<? extends AbstractModule>> getExtraModulesFor(final Class<?> clazz) throws InitializationError {
        QOSExtraModules annotation = clazz.getAnnotation(QOSExtraModules.class);
        return annotation == null ? Lists.newArrayList() : Lists.newArrayList(annotation.value());
    }

    private AbstractModule getFailCondition(Class<?> clazz) throws InitializationError {
        FailWith failWith = clazz.getAnnotation(FailWith.class);
        if (failWith == null) {
            return new FailWithModule(SimpleFailCondition.class);
        }

        return new FailWithModule(failWith.value());
    }
}