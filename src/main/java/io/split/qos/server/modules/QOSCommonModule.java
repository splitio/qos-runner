package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.split.qos.server.QOSServerConfiguration;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.datadog.DatadogBroadcaster;
import io.split.qos.server.integrations.pagerduty.PagerDutyBroadcaster;
import io.split.qos.server.integrations.slack.SlackSessionProvider;
import io.split.qos.server.stories.QOSStories;
import io.split.testrunner.util.TestsFinder;

/**
 * Module used to inject from the Server Guice Injector into the Test Injectors
 *
 * <p>
 *     Using Guice.createChildInjector is causing troubles since some properties are imported both in the server and in
 *     the tests.
 * </p>
 */
public class QOSCommonModule extends AbstractModule {

    private final SlackSessionProvider slackSessionProvider;
    private final QOSServerState state;
    private final QOSStories stories;
    private final TestsFinder testFinder;
    private final PagerDutyBroadcaster pagetDuty;
    private final QOSServerConfiguration configuration;
    private final DatadogBroadcaster datadogBroadcaster;

    public QOSCommonModule(SlackSessionProvider slackSessionProvider,
                           QOSServerState serverState,
                           QOSStories stories,
                           TestsFinder testsFinder,
                           PagerDutyBroadcaster pagerDutyBroadcaster,
                           QOSServerConfiguration configuration,
                           DatadogBroadcaster datadogBroadcaster) {
        this.slackSessionProvider = Preconditions.checkNotNull(slackSessionProvider);
        this.state = Preconditions.checkNotNull(serverState);
        this.stories = Preconditions.checkNotNull(stories);
        this.testFinder = Preconditions.checkNotNull(testsFinder);
        this.pagetDuty = Preconditions.checkNotNull(pagerDutyBroadcaster);
        this.configuration = Preconditions.checkNotNull(configuration);
        this.datadogBroadcaster = Preconditions.checkNotNull(datadogBroadcaster);
    }

    @Override
    protected void configure() {
        bind(SlackSessionProvider.class).toInstance(slackSessionProvider);
        bind(QOSServerState.class).toInstance(state);
        bind(QOSStories.class).toInstance(stories);
        bind(TestsFinder.class).toInstance(testFinder);
        bind(PagerDutyBroadcaster.class).toInstance(pagetDuty);
        bind(QOSServerConfiguration.class).toInstance(configuration);
        bind(DatadogBroadcaster.class).toInstance(datadogBroadcaster);
    }
}
