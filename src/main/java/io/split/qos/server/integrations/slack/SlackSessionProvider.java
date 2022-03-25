package io.split.qos.server.integrations.slack;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class SlackSessionProvider {
    private static final Logger LOG = LoggerFactory.getLogger(SlackSessionProvider.class);

    private String digestChannel;
    private String verboseChannel;

    /**
     * Initialization of slack connection and slack channels
     *
     * @param slackVerboseChannel name of the verbose channel.
     * @param slackDigestChannel name of the digest channel.
     */
    public synchronized void initialize(String slackVerboseChannel,
                                  String slackDigestChannel) {
        try {
            verboseChannel = slackVerboseChannel;
            if (verboseChannel == null) {
                throw new IllegalArgumentException("Could not find verbose channel " + slackVerboseChannel);
            }

            digestChannel = slackDigestChannel;
            if (digestChannel == null) {
                throw new IllegalArgumentException("Could not find digest channel " + slackDigestChannel);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not connect to slack", e);
        }
    }

    public String digestChannel() {
        return digestChannel;
    }

    public String verboseChannel() { return verboseChannel; }

    public boolean isEnabled() {
        return true;
    }
}
