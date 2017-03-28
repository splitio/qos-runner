package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Optional;

/**
 * Represents a command directed to the slack bot.
 */
public class SlackCommand {

    private final String command;
    private final List<String> arguments;
    private final Optional<String> server;

    /**
     * Default Constructor. User the static get.
     *
     * @param command Command issued.
     * @param arguments Arguments of the command.
     */
    SlackCommand(Optional<String> server, String command, List<String> arguments) {
        this.server = Preconditions.checkNotNull(server);
        this.command = Preconditions.checkNotNull(command);
        this.arguments = Preconditions.checkNotNull(arguments);
    }

    public Optional<String> server() {
        return server;
    }

    public String command() {
        return command;
    }

    public List<String> arguments() {
        return arguments;
    }
}
