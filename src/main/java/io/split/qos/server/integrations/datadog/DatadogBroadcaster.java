package io.split.qos.server.integrations.datadog;

import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.Integration;
import org.junit.runner.Description;

import java.util.Optional;

public interface DatadogBroadcaster extends AutoCloseable, Integration {

    void initialize(QOSServerState serverState, String host, Integer port, String qosServerName);

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

}
