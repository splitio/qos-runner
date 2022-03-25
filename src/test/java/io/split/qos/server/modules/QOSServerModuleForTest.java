package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.broadcaster.SlackTestResultBroadcaster;
import io.split.qos.server.mocks.PagerDutyBroadcasterForTest;
import io.split.qos.server.mocks.SlackTestResultBroadcasterImplForTest;
import io.split.qos.server.util.BroadcasterTestWatcher;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.JUnitRunnerFactory;

public class QOSServerModuleForTest extends AbstractModule {
    // Server Name
    // Default QOS Server
    public static final String QOS_SERVER_NAME = "QOS_SERVER_NAME";
    private final String serverName;

    public QOSServerModuleForTest(String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
    }

    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(JUnitRunner.class, JUnitRunner.class)
                .build(JUnitRunnerFactory.class));

        bind(SlackTestResultBroadcaster.class).to(SlackTestResultBroadcasterImplForTest.class);
        bind(PagerDutyBroadcaster.class).to(PagerDutyBroadcasterForTest.class);
        bindConstant()
                .annotatedWith(Names.named(QOS_SERVER_NAME))
                .to(serverName);
        // HACK. Since we need the server for the tests (for broadcasting) and the tests use a complete different
        // Injector, we use static variables to communicate.
        BroadcasterTestWatcher.serverName = serverName;
    }
}
