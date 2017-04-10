package io.split.qos.server.integrations.slack.commands;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;

import java.util.List;
import java.util.function.BiPredicate;

/**
 * A Command is a BiPredicate that also has a help for displaying purposes.
 */
public interface SlackCommandExecutor extends BiPredicate<SlackMessagePosted, SlackSession> {
    /**
     * Description of the command
     */
    String description();

    /**
     * Arguments that the command accepts.
     */
    String arguments();

    boolean acceptsArguments(List<String> arguments);
}
