package io.split.qos.server;

import com.google.common.base.Preconditions;
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
import io.dropwizard.lifecycle.ServerLifecycleListener;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.split.qos.server.integrations.datadog.DatadogBroadcaster;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.SlackSessionProvider;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.register.QOSRegister;
import io.split.qos.server.resources.BehaviourResource;
import io.split.qos.server.resources.ConfigResource;
import io.split.qos.server.resources.CountResource;
import io.split.qos.server.resources.GreenResource;
import io.split.qos.server.resources.HealthResource;
import io.split.qos.server.resources.StateResource;
import io.split.testrunner.util.GuiceInitializator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * DropWizard Server Application.
 */
public class QOSServerApplication extends Application<QOSServerConfiguration> {
    private static final Logger LOG = LoggerFactory.getLogger(QOSServerApplication.class);

    public static Injector injector;
    private String name;
    public QOSRegister register;
    private PagerDutyBroadcaster pagerDuty;
    private DatadogBroadcaster datadog;

    public static void main(String[] args) throws Exception {
        try {
            new QOSServerApplication().run(args);
        } catch (Throwable t) {
            LOG.error("QOS Server unpexpectedly exited", t);

        }
    }

    @Override
    public void initialize(Bootstrap<QOSServerConfiguration> bootstrap) {
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
        List<Module> modules = Lists.newArrayList(new QOSServerModule(configuration), new QOSPropertiesModule());

        LOG.info("Initializing Guice");
        long begin = System.currentTimeMillis();
        injector = Guice.createInjector(modules);
        LOG.info(String.format("Guice initialized in %s milliseconds", System.currentTimeMillis() - begin));
        environment.jersey().register(new HealthResource());
        environment.jersey().register(new GreenResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new ConfigResource(injector.getInstance(Key.get(Properties.class, Names.named(QOSPropertiesModule.CONFIGURATION)))));
        environment.jersey().register(new StateResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new CountResource(injector.getInstance(QOSServerState.class)));
        environment.jersey().register(new BehaviourResource(injector.getInstance(QOSServerBehaviour.class)));

        QOSServerConfiguration.Register register = configuration.getRegister();
        if (register != null) {
            this.register = injector.getInstance(QOSRegister.class);
            if (Strings.isNullOrEmpty(register.getDashboardURL())) {
                throw new IllegalArgumentException("Register was set in yaml, but not property qosDashboardURL");
            }
            if (!Strings.isNullOrEmpty(register.getHostURL())) {
                environment.lifecycle().addServerLifecycleListener(new ServiceLyfeListener(this, register.getDashboardURL(), Optional.of(register.getHostURL())));
            }
            else{
                environment.lifecycle().addServerLifecycleListener(new ServiceLyfeListener(this, register.getDashboardURL(), Optional.empty()));
            }
        }

        QOSServerConfiguration.PagerDuty pagerDuty = configuration.getPagerDuty();
        if (pagerDuty != null) {
            this.pagerDuty = injector.getInstance(PagerDutyBroadcaster.class);
            if (Strings.isNullOrEmpty(pagerDuty.getServiceKey())) {
                throw new IllegalArgumentException("PagerDuty was set in yaml, but not property serviceKey");
            }
            this.pagerDuty.initialize(pagerDuty.getServiceKey(), name);
        }

        QOSServerConfiguration.Datadog datadog = configuration.getDatadog();
        if (datadog != null) {
            this.datadog = injector.getInstance(DatadogBroadcaster.class);
            QOSServerState serverState = injector.getInstance(QOSServerState.class);
            if (Strings.isNullOrEmpty(datadog.getHost())) {
                throw new IllegalArgumentException("Datadog was set in yaml, but not property host was set");
            }
            if (datadog.getPort() == null) {
                throw new IllegalArgumentException("Datadog was set in yaml, but not property port was set");
            }
            this.datadog.initialize(serverState, datadog.getHost(), datadog.getPort(), name);
        }

        QOSServerConfiguration.Slack slack = configuration.getSlack();
        if (slack!= null) {
            SlackSessionProvider provider = injector.getInstance(SlackSessionProvider.class);
            if (Strings.isNullOrEmpty(slack.getVerboseChannel())) {
                throw new IllegalArgumentException("Slack was set in yaml, but not property verboseChannel");
            }
            if (Strings.isNullOrEmpty(slack.getDigestChannel())) {
                throw new IllegalArgumentException("Slack was set in yaml, but not property digestchannel");
            }
            provider.initialize(slack.getVerboseChannel(), slack.getDigestChannel());
        }

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

    private static class ServiceLyfeListener implements ServerLifecycleListener {

        private final QOSServerApplication serverApplication;
        private final String dashboardURL;
        private final Optional<String> hostURL;

        private ServiceLyfeListener(QOSServerApplication serverApplication, String dashboardURL, Optional<String> hostURL) {
            this.serverApplication = Preconditions.checkNotNull(serverApplication);
            this.dashboardURL = Preconditions.checkNotNull(dashboardURL);
            this.hostURL = Preconditions.checkNotNull(hostURL);
        }

        @Override
        public void serverStarted(Server server) {
            if (hostURL.isPresent()) {
                serverApplication.register.register(dashboardURL, hostURL.get());
            }
            else{
                serverApplication.register.register(dashboardURL, getRunnerURL(server));
            }
        }
    }

    private static String getRunnerURL(Server server) {
        Connector connector = null;
        if (server.getConnectors().length == 1) {
            connector = server.getConnectors()[0];
        } else {
            Optional<Connector> application = Arrays.stream(server.getConnectors())
                    .filter(theConnector -> theConnector.getName().equals("application"))
                    .findAny();
            if (!application.isPresent()) {
                throw new IllegalStateException("Could not find application connector on server " + server);
            }
            connector = application.get();
        }
        if (connector instanceof ServerConnector) {
            ServerConnector serverConnector = (ServerConnector) connector;
            int port = serverConnector.getPort();
            return String.format("http://%s:%s", getHost(), port);
        } else {
            throw new IllegalStateException("Only accepts Server Connectors");
        }
    }

    private static String getHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }

    }
}