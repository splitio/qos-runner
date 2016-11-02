package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

/**
 * Gives that last time all the tests ran and were green.
 *
 * Check QOSServerState for a definition of what 'green' means.
 */
@Singleton
public class SlackGreenCommand implements SlackCommandExecutor {
    private final String serverName;
    private final QOSServerState state;
    private final DateFormatter dateFormatter;
    private final SlackColors colors;

    @Inject
    public SlackGreenCommand(
            QOSServerState state,
            DateFormatter dateFormatter,
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = slackColors;
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.state = state;
    }

    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Long lastGreen = state.lastGreen();
        String title = String.format("[%s] Green", serverName.toUpperCase());
        SlackAttachment slackAttachment = new SlackAttachment(title, "", "", null);
        if (lastGreen == null) {
            slackAttachment.setText("--");
            slackAttachment.setColor(colors.getFailed());
        } else {
            slackAttachment.setText(String.format("Last Green %s %s", dateFormatter.formatDate(lastGreen), state.isPaused() ? "(Paused)" : ""));
            slackAttachment.setColor(colors.getSuccess());
        }
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
        return true;
    }

    @Override
    public String help() {
        return "green [server-name]: Displays the last time when all the tests passed";
    }
}
