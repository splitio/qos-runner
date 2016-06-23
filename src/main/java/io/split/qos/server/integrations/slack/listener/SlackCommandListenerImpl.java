package io.split.qos.server.integrations.slack.listener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commands.SlackCommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Singleton
public class SlackCommandListenerImpl implements SlackCommandListener{
    private static final Logger LOG = LoggerFactory.getLogger(SlackCommandListenerImpl.class);

    private final Map<String, SlackCommandExecutor> commands;

    @Inject
    public SlackCommandListenerImpl() {
        this.commands = Maps.newHashMap();
    }

    @Override
    public void register(String command, SlackCommandExecutor predicate) {
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(predicate);

        commands.put(command.toLowerCase(), predicate);
    }

    @Override
    public void execute(String command, SlackMessagePosted message, SlackSession session) {
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(session);

        SlackCommandExecutor predicate = commands.get(command.toLowerCase());
        if (predicate != null) {
            predicate.test(message, session);
        } else {
            LOG.warn("Could not find command for " + command);
        }
    }

    @Override
    public List<SlackCommandExecutor> commands() {
        return Lists.newArrayList(commands.values());
    }
}
