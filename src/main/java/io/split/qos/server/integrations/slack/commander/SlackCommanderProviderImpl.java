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
    private final Provider<SlackGreenCommand> greenProvider;
    private final Provider<SlackCommandsCommand> commandsProvider;
    private final Provider<SlackFailedCommand> failedProvider;
    private final Provider<SlackMissingCommand> missingProvider;
    private final Provider<SlackRunAllCommand> runAllProvider;
    private final Provider<SlackRunTestCommand> runTestProvider;
    private final Provider<SlackConfigCommand> configProvider;
    private final Provider<SlackPingCommand> pingProvider;
    private final Provider<SlackCountCommand> countProvider;
    private final Provider<SlackStoryCommand> storyProvider;
    private final Provider<SlackTestsCommand> testsProvider;
    private final Provider<SlackSucceededCommand> succeededProvider;

    @Inject
    public SlackCommanderProviderImpl(
            Provider<SlackInfoCommand> infoCommandProvider,
            Provider<SlackPauseCommand> pauseCommandProvider,
            Provider<SlackResumeCommand> resumeCommandProvider,
            Provider<SlackGreenCommand> greenCommandProvider,
            Provider<SlackCommandsCommand> commandsCommandProvider,
            Provider<SlackFailedCommand> failedCommandProvider,
            Provider<SlackMissingCommand> missingCommandProvider,
            Provider<SlackSucceededCommand> succeededCommandProvider,
            Provider<SlackRunAllCommand> runAllCommandProvider,
            Provider<SlackRunTestCommand> runTestCommandProvider,
            Provider<SlackCountCommand> countCommandProvider,
            Provider<SlackConfigCommand> configCommandProvider,
            Provider<SlackPingCommand> pingCommandProvider,
            Provider<SlackTestsCommand> testsCommandProvider,
            Provider<SlackStoryCommand> storyCommandProvider) {
        this.infoProvider = Preconditions.checkNotNull(infoCommandProvider);
        this.pauseProvider = Preconditions.checkNotNull(pauseCommandProvider);
        this.resumeProvider = Preconditions.checkNotNull(resumeCommandProvider);
        this.greenProvider = Preconditions.checkNotNull(greenCommandProvider);
        this.commandsProvider = Preconditions.checkNotNull(commandsCommandProvider);
        this.failedProvider = Preconditions.checkNotNull(failedCommandProvider);
        this.runAllProvider = Preconditions.checkNotNull(runAllCommandProvider);
        this.runTestProvider = Preconditions.checkNotNull(runTestCommandProvider);
        this.configProvider = Preconditions.checkNotNull(configCommandProvider);
        this.pingProvider = Preconditions.checkNotNull(pingCommandProvider);
        this.storyProvider = Preconditions.checkNotNull(storyCommandProvider);
        this.testsProvider = Preconditions.checkNotNull(testsCommandProvider);
        this.missingProvider = Preconditions.checkNotNull(missingCommandProvider);
        this.succeededProvider = Preconditions.checkNotNull(succeededCommandProvider);
        this.countProvider = Preconditions.checkNotNull(countCommandProvider);
    }

    @Override
    public SlackInfoCommand info() {
        return infoProvider.get();
    }

    @Override
    public SlackPauseCommand pause() {
        return pauseProvider.get();
    }

    @Override
    public SlackResumeCommand resume() {
        return resumeProvider.get();
    }

    @Override
    public SlackGreenCommand green() {
        return greenProvider.get();
    }

    @Override
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

    @Override
    public SlackTestsCommand tests() {
        return testsProvider.get();
    }

    @Override
    public SlackStoryCommand story() {
        return storyProvider.get();
    }

    @Override
    public SlackCountCommand count() {
        return countProvider.get();
    }

    @Override
    public SlackSucceededCommand succeeded() {
        return succeededProvider.get();
    }

    @Override
    public SlackMissingCommand missing() {
        return missingProvider.get();
    }

}
