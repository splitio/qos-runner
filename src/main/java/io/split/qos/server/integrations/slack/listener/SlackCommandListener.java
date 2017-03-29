package io.split.qos.server.integrations.slack.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commands.SlackCommandExecutor;

import java.util.List;

/**
 * Encapsulation for registering commands and executing commands.
 */
public interface SlackCommandListener {
    /**
     * Ties a command with the CommandExecutor.
     *
     * @param command the command to be registered.
     * @param predicate the SlackCommandExecutor that will be associated to the command.
     */
    void register(String command, SlackCommandExecutor predicate);

    /**
     * Executes the command if it was registered.
     *
     * @param command the command to be executed.
     * @param message the message where the command was sent.
     * @param session the Slack Session.
     */
    void execute(SlackCommand command, SlackMessagePosted message, SlackSession session);

    /**
     * @return all the registered commands.
     */
    List<SlackCommandExecutor> commands();
}
