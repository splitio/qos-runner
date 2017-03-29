package io.split.qos.server.integrations.slack.commander;

import io.split.qos.server.integrations.slack.commands.*;

/**
 * Provides the different commands.
 *
 * For not clutching all the classes, new commands should be provided here.
 */
public interface SlackCommandProvider {
    SlackInfoCommand info();
    SlackPauseCommand pause();
    SlackResumeCommand resume();
    SlackGreenCommand green();
    SlackCommandsCommand commands();
    SlackFailedCommand failed();
    SlackMissingCommand missing();
    SlackRunAllCommand runAll();
    SlackRunTestCommand runTest();
    SlackConfigCommand config();
    SlackPingCommand ping();
    SlackTestsCommand tests();
    SlackStoryCommand story();
}
