package io.split.qos.server.mocks;

import com.google.inject.Singleton;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import org.junit.runner.Description;

@Singleton
public class SlackBroadcastIntegrationForTest implements SlackBroadcaster {
    @Override
    public void firstFailure(Description description, Throwable error, String serverName, Long duration) {

    }

    @Override
    public void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure, Long duration) {

    }

    @Override
    public void recovery(Description description, String serverName, Long duration) {

    }

    @Override
    public void success(Description description, String serverName, Long duration) {

    }

    @Override
    public void broadcastVerbose(String message, SlackAttachment attachment) {

    }

    @Override
    public void broadcastDigest(String message, SlackAttachment attachment) {

    }

    @Override
    public void initialize() {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public void close() throws Exception {

    }
}
