package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.slack.SlackCommon;

/**
 * Module used to inject from the Server Guice Injector into the Test Injectors
 *
 * <p>
 *     Using Guice.createChildInjector is causing troubles since some properties are imported both in the server and in
 *     the tests.
 * </p>
 */
public class QOSCommonModule extends AbstractModule {

    private final SlackCommon slackCommon;
    private final QOSServerState state;

    public QOSCommonModule(SlackCommon slackCommon,
                           QOSServerState serverState) {
        this.slackCommon = Preconditions.checkNotNull(slackCommon);
        this.state = Preconditions.checkNotNull(serverState);
    }

    @Override
    protected void configure() {
        bind(SlackCommon.class).toInstance(slackCommon);
        bind(QOSServerState.class).toInstance(state);
    }
}
