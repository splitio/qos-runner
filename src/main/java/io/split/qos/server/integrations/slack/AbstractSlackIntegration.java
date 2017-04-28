package io.split.qos.server.integrations.slack;

import com.google.common.base.Preconditions;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import io.split.qos.server.integrations.Integration;

import java.io.IOException;

/**
 * Common behavior of a Slack Integration.
 */
public abstract class AbstractSlackIntegration implements Integration {
    private final String token;
    private final String slackDigestChannel;
    private final String slackVersboseChannel;
    private final SlackCommon slackCommon;
    private boolean digestEnabled;
    private boolean verboseEnabled;
    private SlackSession slackSession;
    private SlackChannel verboseChannel;
    private SlackChannel digestChannel;
    private String botId;

    public AbstractSlackIntegration(
            String slackBotToken,
            String slackDigestChannel,
            String slackVerboseChannel,
            SlackCommon slackCommon) {
        this.token = Preconditions.checkNotNull(slackBotToken);
        this.slackCommon = Preconditions.checkNotNull(slackCommon);
        this.slackDigestChannel = Preconditions.checkNotNull(slackDigestChannel);
        this.slackVersboseChannel = Preconditions.checkNotNull(slackVerboseChannel);
    }

    protected void initialize(boolean isServer) throws IOException {
        slackCommon.initialize(
                token,
                slackVersboseChannel,
                slackDigestChannel,
                isServer
        );
        digestEnabled = slackCommon.digestEnabled();
        verboseEnabled = slackCommon.verboseEnabled();
        slackSession = slackCommon.slackSession();
        verboseChannel = slackCommon.verboseChannel();
        digestChannel = slackCommon.digestChannel();
        botId = slackCommon.botId();
    }

    public SlackChannel digestChannel() {
        return digestChannel;
    }

    public SlackChannel verboseChannel() {
        return verboseChannel;
    }

    public SlackSession slackSession() {
        return slackSession;
    }

    public boolean digestEnabled() {
        return digestEnabled;
    }

    public boolean verboseEnabled() {
        return verboseEnabled;
    }

    public boolean isInitializedByServer() {
        return slackCommon.isInitializedByServer();
    }

    public String botId() {
        return botId;
    }

    protected void close(boolean isServer) throws Exception {
        slackCommon.close(isServer);
    }
}
