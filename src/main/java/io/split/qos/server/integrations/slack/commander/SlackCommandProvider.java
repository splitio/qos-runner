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
    SlackTestsCommand tests();
    SlackGreenCommand green();
    SlackCommandsCommand commands();
    SlackFailedCommand failed();
    SlackRunAllCommand runAll();
    SlackRunTestCommand runTest();
    SlackConfigCommand config();
    SlackPingCommand ping();
}
