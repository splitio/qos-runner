package io.split.qos.server.mocks;

import com.google.common.collect.Lists;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commands.SlackCommandExecutor;
import io.split.qos.server.integrations.slack.listener.SlackCommandListener;

import java.util.List;

public class SlackCommandListenerForTest implements SlackCommandListener {
    @Override
    public void register(String command, SlackCommandExecutor predicate) {

    }

    @Override
    public void execute(String command, SlackMessagePosted message, SlackSession session) {

    }

    @Override
    public List<SlackCommandExecutor> commands() {
        return Lists.newArrayList();
    }
}
