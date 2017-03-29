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
     * @return Some handy information on how to use the command.
     */
    String help();

    boolean acceptsArguments(List<String> arguments);
}
