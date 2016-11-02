package io.split.qos.server.integrations.slack.commander;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.commands.*;

@Singleton
public class SlackCommanderProviderImpl implements SlackCommandProvider {
    private final Provider<SlackInfoCommand> infoProvider;
    private final Provider<SlackPauseCommand> pauseProvider;
    private final Provider<SlackResumeCommand> resumeProvider;
    private final Provider<SlackTestsCommand> testsProvider;
    private final Provider<SlackGreenCommand> greenProvider;
    private final Provider<SlackCommandsCommand> commandsProvider;
    private final Provider<SlackFailedCommand> failedProvider;
    private final Provider<SlackRunAllCommand> runAllProvider;
    private final Provider<SlackRunTestCommand> runTestProvider;
    private final Provider<SlackConfigCommand> configProvider;
    private final Provider<SlackPingCommand> pingProvider;

    @Inject
    public SlackCommanderProviderImpl(
            Provider<SlackInfoCommand> infoCommandProvider,
            Provider<SlackPauseCommand> pauseCommandProvider,
            Provider<SlackResumeCommand> resumeCommandProvider,
            Provider<SlackTestsCommand> testsCommandProvider,
            Provider<SlackGreenCommand> greenCommandProvider,
            Provider<SlackCommandsCommand> commandsCommandProvider,
            Provider<SlackFailedCommand> failedCommandProvider,
            Provider<SlackRunAllCommand> runAllCommandProvider,
            Provider<SlackRunTestCommand> runTestCommandProvider,
            Provider<SlackConfigCommand> configCommandProvider,
            Provider<SlackPingCommand> pingCommandProvider) {
        this.infoProvider = Preconditions.checkNotNull(infoCommandProvider);
        this.pauseProvider = Preconditions.checkNotNull(pauseCommandProvider);
        this.resumeProvider = Preconditions.checkNotNull(resumeCommandProvider);
        this.testsProvider = Preconditions.checkNotNull(testsCommandProvider);
        this.greenProvider = Preconditions.checkNotNull(greenCommandProvider);
        this.commandsProvider = Preconditions.checkNotNull(commandsCommandProvider);
        this.failedProvider = Preconditions.checkNotNull(failedCommandProvider);
        this.runAllProvider = Preconditions.checkNotNull(runAllCommandProvider);
        this.runTestProvider = Preconditions.checkNotNull(runTestCommandProvider);
        this.configProvider = Preconditions.checkNotNull(configCommandProvider);
        this.pingProvider = Preconditions.checkNotNull(pingCommandProvider);
    }

    public SlackInfoCommand info() {
        return infoProvider.get();
    }

    public SlackPauseCommand pause() {
        return pauseProvider.get();
    }

    public SlackResumeCommand resume() {
        return resumeProvider.get();
    }

    public SlackTestsCommand tests() {
        return testsProvider.get();
    }

    public SlackGreenCommand green() {
        return greenProvider.get();
    }

    public SlackCommandsCommand commands() {
        return commandsProvider.get();
    }

    @Override
    public SlackFailedCommand failed() {
        return failedProvider.get();
    }

    @Override
    public SlackRunAllCommand runAll() {
        return runAllProvider.get();
    }

    @Override
    public SlackRunTestCommand runTest() {
        return runTestProvider.get();
    }

    @Override
    public SlackConfigCommand config() {
        return configProvider.get();
    }

    @Override
    public SlackPingCommand ping() {
        return pingProvider.get();
    }
}
