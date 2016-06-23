package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.split.qos.server.mocks.SlackBroadcastIntegrationForTest;
import io.split.qos.server.mocks.SlackCommandIntegrationForTest;
import io.split.qos.server.mocks.SlackCommandListenerForTest;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegration;
import io.split.qos.server.integrations.slack.commander.SlackCommandProvider;
import io.split.qos.server.integrations.slack.commander.SlackCommanderProviderImpl;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;
import io.split.qos.server.testrunner.QOSTestRunner;
import io.split.qos.server.testrunner.QOSTestRunnerFactory;
import io.split.qos.server.util.BroadcasterTestWatcher;

public class QOSServerModuleForTest extends AbstractModule {
    private final String serverName;

    // Server Name
    // Default QOS Server
    public static final String QOS_SERVER_NAME = "QOS_SERVER_NAME";

    public QOSServerModuleForTest(String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(QOSTestRunner.class, QOSTestRunner.class)
                .build(QOSTestRunnerFactory.class));

        bind(SlackCommandProvider.class).to(SlackCommanderProviderImpl.class);
        bind(SlackCommandIntegration.class).to(SlackCommandIntegrationForTest.class);
        bind(SlackBroadcaster.class).to(SlackBroadcastIntegrationForTest.class);
        bind(SlackCommandListener.class).to(SlackCommandListenerForTest.class);
        bindConstant()
                .annotatedWith(Names.named(QOS_SERVER_NAME))
                .to(serverName);
        // HACK. Since we need the server for the tests (for broadcasting) and the tests use a complete different
        // Injector, we use static variables to communicate.
        BroadcasterTestWatcher.serverName = serverName;
    }
}
