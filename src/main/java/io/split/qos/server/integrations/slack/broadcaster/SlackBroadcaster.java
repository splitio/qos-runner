package io.split.qos.server.integrations.slack.broadcaster;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import io.split.qos.server.integrations.Integration;
import org.junit.runner.Description;

import java.util.Optional;

/**
 * In charge of broadcasting success/failures etc.
 */
public interface SlackBroadcaster extends AutoCloseable, Integration {
    /**
     * Broadcasts the first failure
     *
     * @param description the test description.
     * @param error the error that caused the failure.
     */
    void firstFailure(Description description, Throwable error, String serverName, Long duration, Optional<String> titleLink);

    /**
     * Re broadcasts a failure
     *
     * @param description the test description.
     * @param error the error that caused the failure.
     */
    void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure, Long duration, Optional<String> titleLink);

    /**
     * Broadcasts that a test that was failing now succeeded.
     *
     * @param description the test description.
     */
    void recovery(Description description, String serverName, Long duration, Optional<String> titleLink);

    /**
     * Broadcasts that a test has succeeded.
     *
     * @param description the test description.
     */
    void success(Description description, String serverName, Long duration, Optional<String> titleLink);

    /**
     * Broadcasts a message to the verbose channel.
     *
     * @param message the message to send.
     * @param attachment the slack attachment (THIS MEANS IT IS IN IN THE WRONG PLACE)
     */
    void broadcastVerbose(String message, SlackAttachment attachment);

    /**
     * Broadcasts a message to the digest channel.
     *
     * @param message the message to send.
     * @param attachment the slack attachment (THIS MEANS IT IS IN IN THE WRONG PLACE)
     */
    void broadcastDigest(String message, SlackAttachment attachment);

    /**
     * Initializes the broadcaster.
     */
    void initialize();
}
