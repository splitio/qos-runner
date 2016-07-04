package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.failcondition.SimpleFailCondition;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcastIntegrationImpl;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegration;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegrationImpl;
import io.split.qos.server.integrations.slack.commander.SlackCommandProvider;
import io.split.qos.server.integrations.slack.commander.SlackCommanderProviderImpl;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.integrations.slack.listener.SlackCommandListenerImpl;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.JUnitRunnerFactory;
import io.split.qos.server.util.BroadcasterTestWatcher;

/**
 * Module for installing Server related Guice injections.
 */
public class QOSServerModule extends AbstractModule {
    private final String serverName;

    // Server Name
    // Default QOS Server
    public static final String QOS_SERVER_NAME = "QOS_SERVER_NAME";

    public QOSServerModule(String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(JUnitRunner.class, JUnitRunner.class)
                .build(JUnitRunnerFactory.class));

        bind(FailCondition.class).to(SimpleFailCondition.class).in(Singleton.class);
        bind(SlackCommandProvider.class).to(SlackCommanderProviderImpl.class);
        bind(SlackCommandIntegration.class).to(SlackCommandIntegrationImpl.class);
        bind(SlackBroadcaster.class).to(SlackBroadcastIntegrationImpl.class);
        bind(SlackCommandListener.class).to(SlackCommandListenerImpl.class);
        bindConstant()
                .annotatedWith(Names.named(QOS_SERVER_NAME))
                .to(serverName);
        // HACK. Since we need the server for the tests (for broadcasting) and the tests use a complete different
        // Injector, we use static variables to communicate.
        BroadcasterTestWatcher.serverName = serverName;
    }
}
