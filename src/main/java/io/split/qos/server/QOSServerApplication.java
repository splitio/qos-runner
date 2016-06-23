package io.split.qos.server;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.resources.HealthResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * DropWizard Server Application.
 */
public class QOSServerApplication extends Application<QOSServerConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(QOSServerApplication.class);

    public static Injector injector;
    private String name;

    public static void main(String[] args) throws Exception {
        new QOSServerApplication().run(args);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void run(QOSServerConfiguration configuration, Environment environment) throws Exception {
        name = configuration.getServerName();

        // HACK so it can be loaded by the tests, since they use another guice injector.
        LOG.info("Setting Conf variable to: " + configuration.getConfig());
        System.setProperty(QOSPropertiesModule.CONF, configuration.getConfig());

        //Not IDEAL but have to keep compatibility with DropWizard 0.9.2, so cannot use
        //GuiceWizard.
        List<Module> modules = Lists.newArrayList(
                new QOSPropertiesModule(),
                new QOSServerModule(configuration.getServerName())
        );

        injector = Guice.createInjector(modules);
        environment.jersey().register(new HealthResource(injector.getInstance(QOSServerState.class)));

        QOSServerBehaviour behaviour = injector.getInstance(QOSServerBehaviour.class);
        behaviour.call();

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