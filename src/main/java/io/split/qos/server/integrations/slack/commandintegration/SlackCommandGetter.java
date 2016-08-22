package io.split.qos.server.integrations.slack.commandintegration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.SlackCommon;

import java.util.List;
import java.util.Optional;

@Singleton
public class SlackCommandGetter {

    private final SlackCommon slackCommon;

    @Inject
    public SlackCommandGetter(SlackCommon slackCommon) {
        this.slackCommon = Preconditions.checkNotNull(slackCommon);
    }

    public Optional<SlackCommand> get(SlackMessagePosted message) {
        String commandWithArgs = stripBotId(slackCommon, message);
        List<String> all = Lists.newArrayList(commandWithArgs.split(" "));
        if (all.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new SlackCommand(all.remove(0), all));
    }

    private String stripBotId(SlackCommon slackCommon, SlackMessagePosted message) {
        return message
                .getMessageContent()
                .trim()
                .replaceFirst(String.format("<@%s>", slackCommon.botId()), "")
                .replaceFirst(":", "")
                .trim();
    }
}
