package io.split.qos.server;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.register.QOSRegister;
import io.split.qos.server.resources.ConfigResource;
import io.split.qos.server.resources.GreenResource;
import io.split.qos.server.resources.HealthResource;
import io.split.testrunner.util.GuiceInitializator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * DropWizard Server Application.
 */
public class QOSServerApplication extends Application<QOSServerConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(QOSServerApplication.class);

    public static Injector injector;
    private String name;
    private QOSRegister register;

    public static void main(String[] args) throws Exception {
        new QOSServerApplication().run(args);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run(QOSServerConfiguration configuration, Environment environment) throws Exception {
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
        GuiceInitializator.setQos();
        List<Module> modules = Lists.newArrayList(
                new QOSPropertiesModule(),
                new QOSServerModule(configuration.getServerName())
        );

        LOG.info("Initializing Guice");
        long begin = System.currentTimeMillis();
        injector = Guice.createInjector(modules);
        LOG.info(String.format("Guice initialized in %s milliseconds", System.currentTimeMillis() - begin));
        environment.jersey().register(new HealthResource());
        environment.jersey().register(new GreenResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new ConfigResource(injector.getInstance(Key.get(Properties.class, Names.named(QOSPropertiesModule.CONFIGURATION)))));

        QOSServerBehaviour behaviour = injector.getInstance(QOSServerBehaviour.class);
        behaviour.call();

        QOSServerConfiguration.Register register = configuration.getRegister();
        if (register != null) {
            this.register = injector.getInstance(QOSRegister.class);
            if (Strings.isNullOrEmpty(register.getQosDashboardURL())) {
                throw new IllegalArgumentException("Register was set in yaml, but not property qosDashboardURL");
            }
            if (Strings.isNullOrEmpty(register.getQosRunnerURL())) {
                throw new IllegalArgumentException("Register was set in yaml, but not property qosRunnerURL");
            }
            this.register.register(register.getQosDashboardURL(), register.getQosRunnerURL());
        }


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