package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.SlackSessionProvider;
import io.split.qos.server.modules.QOSServerModule;

import java.util.List;
import java.util.Optional;

@Singleton
public class SlackCommandGetter {

    private final SlackSessionProvider slackSessionProvider;
    private final String serverName;

    @Inject
    public SlackCommandGetter(
            SlackSessionProvider slackSessionProvider,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.slackSessionProvider= Preconditions.checkNotNull(slackSessionProvider);
        this.serverName = Preconditions.checkNotNull(serverName);
    }

    public Optional<SlackCommand> get(SlackMessagePosted message) {
        String commandWithArgs = stripBotId(message);
        List<String> all = Lists.newArrayList(commandWithArgs.split(" "));
        if (all.isEmpty()) {
            return Optional.empty();
        }
        Optional<String> serverNameOptional = Optional.empty();
        if (all.get(0).equalsIgnoreCase(serverName)) {
            serverNameOptional.of(all.remove(0));
        }
        if (all.isEmpty()) {
            return Optional.empty();
        }


        return Optional.of(new SlackCommand(serverNameOptional, all.remove(0), all));
    }

    private String stripBotId(SlackMessagePosted message) {
        return message
                .getMessageContent()
                .trim()
                .replaceFirst(String.format("<@%s>", slackSessionProvider.botId()), "")
                .replaceFirst(":", "")
                .trim();
    }
}
