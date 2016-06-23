package io.split.qos.server.integrations.slack.commandintegration;

import java.util.List;

/**
 * Represents a command directed to the slack bot.
 */
public class SlackCommand {

    private final String command;
    private final List<String> arguments;

    /**
     * Default Constructor. User the static get.
     *
     * @param command Command issued.
     * @param arguments Arguments of the command.
     */
    SlackCommand(String command, List<String> arguments) {
        this.command = command;
        this.arguments = arguments;
    }

    public String command() {
        return command;
    }

    public List<String> arguments() {
        return arguments;
    }
}
