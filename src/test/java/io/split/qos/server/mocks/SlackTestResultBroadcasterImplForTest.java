package io.split.qos.server.mocks;

import com.google.inject.Singleton;
import io.split.qos.server.integrations.slack.broadcaster.SlackTestResultBroadcaster;
import org.junit.runner.Description;

import java.util.Optional;

@Singleton
public class SlackTestResultBroadcasterImplForTest implements SlackTestResultBroadcaster {
    @Override
    public void firstFailure(Description description, Throwable error, String serverName, Long duration,
                             Optional<String> titleLink) {

    }

    @Override
    public void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure,
                                   Long duration, Optional<String> titleLink) {

    }

    @Override
    public void recovery(Description description, String serverName, Long duration, Optional<String> titleLink) {

    }

    @Override
    public void success(Description description, String serverName, Long duration, Optional<String> titleLink) {

    }

    @Override
    public void broadcastVerbose(String message) {

    }

    @Override
    public void broadcastDigest(String message) {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

}
