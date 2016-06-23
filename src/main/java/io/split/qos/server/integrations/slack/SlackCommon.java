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
public class SlackCommon {
    private static final Logger LOG = LoggerFactory.getLogger(SlackCommon.class);

    private SlackChannel digestChannel;
    private SlackSession slackSession;
    private SlackChannel verboseChannel;
    private boolean initializedByServer = false;
    private boolean initialized;

    /**
     * Initialization of slack connection and slack channels
     *
     * @param slackBotToken auth token for the bot
     * @param slackVerboseChannel name of the verbose channel.
     * @param slackDigestChannel name of the digest channel.
     * @param isServer whether the one that is initalizaing is the Augmented Server or not.
     */
    public synchronized void initialize(String slackBotToken,
                                  String slackVerboseChannel,
                                  String slackDigestChannel,
                                  boolean isServer) {
        if (initialized) {
            return;
        }
        if (Strings.isNullOrEmpty(slackBotToken)) {
            LOG.warn("No Slack Bot Token, Slack Integration will not broadcast at all");
        } else {
            try {
                slackSession = SlackSessionFactory
                        .createWebSocketSlackSession(slackBotToken);
                slackSession.connect();
                if (Strings.isNullOrEmpty(slackVerboseChannel)) {
                    LOG.warn("No Slack Verbose Channel, Slack Integration will not broadcast success/failures");
                } else {
                    verboseChannel = slackSession.findChannelByName(slackVerboseChannel);
                    if (verboseChannel == null) {
                        LOG.warn(String.format("Verbose Channel %s not found, Slack Integration will not broadcast success/failures", slackVerboseChannel));
                    } else {
                        slackSession
                                .joinChannel(slackVerboseChannel);
                    }
                }
                if (Strings.isNullOrEmpty(slackDigestChannel)) {
                    LOG.warn("No Slack Digest Channel, Slack Integration will not broadcast summaries");
                } else {
                    digestChannel = slackSession.findChannelByName(slackDigestChannel);
                    if (digestChannel == null) {
                        LOG.warn(String.format("Digest Channel %s not found, Slack Integration will not broadcast summaries", slackDigestChannel));
                    } else {
                        slackSession
                                .joinChannel(slackDigestChannel);
                    }
                }
            } catch (IOException e) {
                LOG.warn(String.format("Could not create session with token %s, Slack Integration will not broadcast at all", slackBotToken), e);
            }
        }
        if (isServer) {
            initializedByServer = true;
        }
        initialized = true;
    }

    public boolean verboseEnabled() {
        return slackSession != null && verboseChannel != null;
    }

    public boolean digestEnabled() {
        return slackSession != null && digestChannel != null;
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

    public String botId() {
        if (slackSession != null) {
            return slackSession.sessionPersona().getId();
        } else {
            return "";
        }
    }

    public boolean isInitializedByServer() {
        return initializedByServer;
    }

    public synchronized void close(boolean isServer) throws IOException {
        if (!isServer && initializedByServer) {
            return;
        }
        if (slackSession != null && slackSession.isConnected()) {
            slackSession.disconnect();
        }
    }
}
