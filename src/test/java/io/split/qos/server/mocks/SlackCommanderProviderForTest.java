package io.split.qos.server.mocks;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.commander.SlackCommandProvider;
import io.split.qos.server.integrations.slack.commands.*;

@Singleton
public class SlackCommanderProviderForTest implements SlackCommandProvider {
    @Inject
    public SlackCommanderProviderForTest() {

    }

    @Override
    public SlackInfoCommand info() {
        return null;
    }

    @Override
    public SlackPauseCommand pause() {
        return null;
    }

    @Override
    public SlackResumeCommand resume() {
        return null;
    }

    @Override
    public SlackGreenCommand green() {
        return null;
    }

    @Override
    public SlackCommandsCommand commands() {
        return null;
    }

    @Override
    public SlackFailedCommand failed() {
        return null;
    }

    @Override
    public SlackMissingCommand missing() {
        return null;
    }

    @Override
    public SlackRunAllCommand runAll() {
        return null;
    }

    @Override
    public SlackRunTestCommand runTest() {
        return null;
    }

    @Override
    public SlackConfigCommand config() {
        return null;
    }

    @Override
    public SlackPingCommand ping() {
        return null;
    }

    @Override
    public SlackTestsCommand tests() {
        return null;
    }

    @Override
    public SlackStoryCommand story() {
        return null;
    }

    @Override
    public SlackCountCommand count() {
        return null;
    }

    @Override
    public SlackSucceededCommand succeeded() {
        return null;
    }

}
