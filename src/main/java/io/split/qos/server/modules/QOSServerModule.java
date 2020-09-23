package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.split.qos.server.QOSServerConfiguration;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.failcondition.SimpleFailCondition;
import io.split.qos.server.integrations.datadog.DatadogBroadcaster;
import io.split.qos.server.integrations.datadog.DatadogBroadcasterImpl;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcasterImpl;
import io.split.qos.server.integrations.slack.broadcaster.SlackTestResultBroacasterImpl;
import io.split.qos.server.integrations.slack.broadcaster.SlackTestResultBroadcaster;
import io.split.qos.server.integrations.slack.commander.SlackCommandProvider;
import io.split.qos.server.integrations.slack.commander.SlackCommanderProviderImpl;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegration;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegrationImpl;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.integrations.slack.listener.SlackCommandListenerImpl;
import io.split.qos.server.util.BroadcasterTestWatcher;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.JUnitRunnerFactory;

/**
 * Module for installing Server related Guice injections.
 */
public class QOSServerModule extends AbstractModule {
    private final String serverName;

    // Server Name
    // Default QOS Server
    public static final String QOS_SERVER_NAME = "QOS_SERVER_NAME";
    private final QOSServerConfiguration configuration;

    public QOSServerModule(QOSServerConfiguration configuration) {
        this.configuration = Preconditions.checkNotNull(configuration);
        this.serverName = Preconditions.checkNotNull(configuration.getServerName());
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(JUnitRunner.class, JUnitRunner.class)
                .build(JUnitRunnerFactory.class));

        bind(FailCondition.class).to(SimpleFailCondition.class).in(Singleton.class);
        bind(SlackCommandProvider.class).to(SlackCommanderProviderImpl.class);
        bind(SlackCommandIntegration.class).to(SlackCommandIntegrationImpl.class);
        bind(SlackTestResultBroadcaster.class).to(SlackTestResultBroacasterImpl.class);
        bind(SlackCommandListener.class).to(SlackCommandListenerImpl.class);
        bind(PagerDutyBroadcaster.class).to(PagerDutyBroadcasterImpl.class);
        bind(DatadogBroadcaster.class).to(DatadogBroadcasterImpl.class);
        bindConstant()
                .annotatedWith(Names.named(QOS_SERVER_NAME))
                .to(serverName);
        bind(QOSServerConfiguration.class).toInstance(configuration);
        // HACK. Since we need the server for the tests (for broadcasting) and the tests use a complete different
        // Injector, we use static variables to communicate.
        BroadcasterTestWatcher.serverName = serverName;
    }
}
