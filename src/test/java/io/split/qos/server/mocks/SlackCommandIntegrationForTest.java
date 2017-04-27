package io.split.qos.server.mocks;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegration;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandRegisterer;

@Singleton
public class SlackCommandIntegrationForTest implements SlackCommandIntegration {
    private final SlackCommandRegisterer registerer;

    @Inject
    public SlackCommandIntegrationForTest(SlackCommandRegisterer registerer) {
        this.registerer = Preconditions.checkNotNull(registerer);
    }

    @Override
    public void initialize() {}

    @Override
    public void startBotListener() {
        registerer.register();
    }

    @Override
    public void close() throws Exception {

    }
}
