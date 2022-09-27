package io.split.qos.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.split.qos.server.failcondition.SimpleFailCondition;
import io.split.qos.server.modules.QOSFailWithModule;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModuleForTest;
import io.split.qos.server.resources.*;
import io.split.testrunner.util.GuiceInitializator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class QOSServerApplicationForTest extends Application<QOSServerConfigurationForTest> {
    private static final Logger LOG = LoggerFactory.getLogger(QOSServerApplicationForTest.class);

    public static Injector injector;
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void initialize(Bootstrap<QOSServerConfigurationForTest> bootstrap) {
        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        super.initialize(bootstrap);
    }

    @Override
    public void run(QOSServerConfigurationForTest configuration, Environment environment) throws Exception {
        GuiceInitializator.initialize();
        name = configuration.getServerName();

        String config = configuration.getConfig();
        // HACK so it can be loaded by the tests, since they use another guice injector.
        LOG.info("Setting Confs to: " + config);
        if (Strings.isNullOrEmpty(config)) {
            throw new IllegalArgumentException("Please set Config in the yaml");
        }
        List<Path> toBeAdded = Arrays.stream(config.split(","))
                .filter(s -> !Strings.isNullOrEmpty(s))
                .map(s -> Paths.get(s))
                .collect(Collectors.toList());
        GuiceInitializator.addAllPaths(toBeAdded);
        //Not IDEAL but have to keep compatibility with DropWizard 0.9.2, so cannot use
        //GuiceWizard.
        List<Module> modules = Lists.newArrayList(
                new QOSPropertiesModule(),
                new QOSServerModuleForTest(configuration.getServerName()),
                new QOSFailWithModule(SimpleFailCondition.class)
        );

        injector = Guice.createInjector(modules);
        environment.jersey().register(new HealthResource());
        environment.jersey().register(new GreenResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new ConfigResource(injector.getInstance(Key.get(Properties.class, Names.named(QOSPropertiesModule.CONFIGURATION)))));
        environment.jersey().register(new StateResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new CountResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new BehaviourResource(injector.getInstance(QOSServerBehaviour.class)));
        environment.jersey().register(new RunningResource(injector.getInstance(QOSTestsTracker.class)));

        QOSServerBehaviour behaviour = injector.getInstance(QOSServerBehaviour.class);

        // Not so nice way to shut down the Slack Connection.
        // Could not figure it ouw a cleaner way.
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Shutting down server");
                try {
                    behaviour.close();
                } catch (Exception e) {
                    LOG.error("Could not shut down server", e);
                }
            }
        });
    }
}
