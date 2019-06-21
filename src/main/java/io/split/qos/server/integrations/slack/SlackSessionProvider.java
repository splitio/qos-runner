package io.split.qos.server.integrations.slack;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Singleton
public class SlackSessionProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SlackSessionProvider.class);

    private SlackChannel digestChannel;
    private SlackSession slackSession;
    private SlackChannel verboseChannel;
    private SlackChannel alertChannel;

    /**
     * Initialization of slack connection and slack channels
     *
     * @param slackBotToken auth token for the bot
     * @param slackVerboseChannel name of the verbose channel.
     * @param slackDigestChannel name of the digest channel.
     */
    public synchronized void initialize(String slackBotToken,
                                  String slackVerboseChannel,
                                  String slackDigestChannel,
                                  String slackAlertChannel) {
        if (Strings.isNullOrEmpty(slackBotToken)) {
            LOG.warn("No Slack Bot Token, Slack Integration will not broadcast at all");
        } else {
            try {
                slackSession = SlackSessionFactory
                        .createWebSocketSlackSession(slackBotToken);
                slackSession.connect();
                verboseChannel = slackSession.findChannelByName(slackVerboseChannel);
                if (verboseChannel == null) {
                    throw new IllegalArgumentException("Could not find verbose channel " + slackVerboseChannel);
                } else {
                    slackSession
                    .joinChannel(slackVerboseChannel);
                }
                digestChannel = slackSession.findChannelByName(slackDigestChannel);
                if (digestChannel == null) {
                    throw new IllegalArgumentException("Could not find digest channel " + slackDigestChannel);
                } else {
                    slackSession
                            .joinChannel(slackDigestChannel);
                }
                alertChannel = slackSession.findChannelByName(slackAlertChannel);
                if (alertChannel == null) {
                    throw new IllegalArgumentException("Could not find alert channel " + slackAlertChannel);
                } else {
                    slackSession
                            .joinChannel(slackAlertChannel);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Could not connect to slack", e);
            }
        }
    }

    public SlackSession slackSession() {
        return slackSession;
    }

    public SlackChannel digestChannel() {
        return digestChannel;
    }

    public SlackChannel verboseChannel() {
        return verboseChannel;
    }


    public SlackChannel alertChannel() {
        return alertChannel;
    }

    public String botId() {
        if (slackSession != null && slackSession.sessionPersona() != null) {
            return slackSession.sessionPersona().getId();
        } else {
            return "";
        }
    }

    public synchronized void close(boolean isServer) throws IOException {
        if (slackSession != null && slackSession.isConnected()) {
            slackSession.disconnect();
        }
    }

    public boolean isEnabled() {
        return slackSession != null;
    }
}
