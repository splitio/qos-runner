package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.commander.SlackCommandProvider;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;

@Singleton
public class SlackCommandRegisterer {

    private final SlackCommandListener listener;
    private final SlackCommandProvider provider;

    @Inject
    public SlackCommandRegisterer(
            SlackCommandListener listener,
            SlackCommandProvider provider) {
        this.listener = Preconditions.checkNotNull(listener);
        this.provider = Preconditions.checkNotNull(provider);
    }

    public void register() {
        listener.register("info", provider.info());
        listener.register("pause", provider.pause());
        listener.register("resume", provider.resume());
        listener.register("tests", provider.tests());
        listener.register("green", provider.green());
        listener.register("commands", provider.commands());
        listener.register("failed", provider.failed());
        listener.register("runall", provider.runAll());
        listener.register("run", provider.runTest());
        listener.register("config", provider.config());
        listener.register("ping", provider.ping());
    }
}
