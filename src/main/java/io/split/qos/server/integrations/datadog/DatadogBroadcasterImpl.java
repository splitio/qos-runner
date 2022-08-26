package io.split.qos.server.integrations.datadog;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import io.split.qos.server.QOSServerState;
import org.junit.runner.Description;

import java.util.Optional;

@Singleton
public class DatadogBroadcasterImpl implements DatadogBroadcaster {

    private String host;
    private Integer port;
    private String qosServerName;
    private StatsDClient statsDClient;
    private QOSServerState serverState;

    @Inject
    public DatadogBroadcasterImpl() {

    }

    @Override
    public boolean isEnabled() {
        return host != null && port != null;
    }

    @Override
    public void close() throws Exception { }

    @Override
    public void initialize(QOSServerState serverState, String host, Integer port, String qosServerName) {
        this.host = host;
        this.port = port;
        this.qosServerName = Preconditions.checkNotNull(qosServerName);
        this.statsDClient = new NonBlockingStatsDClient("qos", host, port);
        this.serverState = serverState;
    }

    @Override
    public void firstFailure(Description description, Throwable error, String serverName, Long duration, Optional<String> titleLink) {
        String[] tags = Lists.newArrayList("servername:"+ serverName).toArray(new String[1]);
        this.statsDClient.count("test.firstfail", 1,tags);

        reportServerState();
    }

    @Override
    public void reBroadcastFailure(Description description, Throwable error, String serverName, Long whenFirstFailure, Long duration, Optional<String> titleLink) {
        String[] tags = Lists.newArrayList("servername:"+ serverName).toArray(new String[1]);
        this.statsDClient.count("test.fail", 1, tags);
        reportServerState();
    }

    @Override
    public void recovery(Description description, String serverName, Long duration, Optional<String> titleLink) {
        reportServerState();
    }

    @Override
    public void success(Description description, String serverName, Long duration, Optional<String> titleLink) {
        String[] tags = Lists.newArrayList("servername:"+ serverName, "length:"+duration.toString()).toArray(new String[2]);
        this.statsDClient.count("test.success", 1, tags);
        reportServerState();
    }

    private void reportServerState() {
        String[] tags = Lists.newArrayList("servername:"+ this.qosServerName).toArray(new String[1]);
        this.statsDClient.gauge("all.succeededTests", serverState.succeededTests().size(), tags);
        this.statsDClient.gauge("all.missingTests", serverState.missingTests().size(), tags);
        this.statsDClient.gauge("all.failingTests", serverState.failedTests().size(), tags);
    }

}
