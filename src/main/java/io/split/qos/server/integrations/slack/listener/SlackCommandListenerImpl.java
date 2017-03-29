package io.split.qos.server.integrations.slack.listener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commands.SlackCommandExecutor;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.SlackColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

@Singleton
public class SlackCommandListenerImpl implements SlackCommandListener {
    private static final Logger LOG = LoggerFactory.getLogger(SlackCommandListenerImpl.class);

    private final Map<String, SlackCommandExecutor> commands;
    private final String serverName;
    private final SlackMessageSender messageSender;
    private final SlackColors colors;

    @Inject
    public SlackCommandListenerImpl(@Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
                                    SlackMessageSender messageSender,
                                    SlackColors colors) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.messageSender = Preconditions.checkNotNull(messageSender);
        this.commands = Maps.newHashMap();
        this.colors = Preconditions.checkNotNull(colors);
    }

    @Override
    public void register(String command, SlackCommandExecutor predicate) {
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(predicate);

        commands.put(command.toLowerCase(), predicate);
    }

    @Override
    public void execute(SlackCommand command, SlackMessagePosted message, SlackSession session) {
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(message);
        Preconditions.checkNotNull(session);

        SlackCommandExecutor predicate = commands.get(command.command().toLowerCase());
        if (predicate != null) {
            if (predicate.acceptsArguments(command.arguments())) {
                predicate.test(message, session);
            } else {
                messageSender
                        .sendWarning(command.command(),
                                "Wrong arguments " + command.arguments(),
                                message.getChannel(),
                                session);
                LOG.warn(String.format("Wrong arguments for command %s, arguments %s", command.command(), command.arguments()));
            }
        } else {
            messageSender
                    .sendWarning(command.command(),
                            "Command not found: " + command.command(),
                            message.getChannel(),
                            session);
            LOG.warn("Could not find command for " + command);

        }
    }

    @Override
    public List<SlackCommandExecutor> commands() {
        return Lists.newArrayList(commands.values());
    }
}
