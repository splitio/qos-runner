package io.split.qos.server.integrations.slack.commandintegration;

import io.split.qos.server.integrations.Integration;

/**
 * Integration for receiving commands aimed to the qosbot and executing those
 * commands.
 */
public interface SlackCommandIntegration extends Integration {
    void initialize();
    /**
     * Starts listening on Slack and executes commands whenever qosbot is referenced.
     */
    void startBotListener();
}
