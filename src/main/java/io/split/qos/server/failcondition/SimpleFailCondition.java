package io.split.qos.server.failcondition;

import com.google.common.base.Preconditions;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.server.QOSServerConfiguration;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.util.TestId;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Simple implementation of a failcondition.
 *
 * It takes into account two things, how many consecutive failures have to happen before broadcasting the failure.
 * And how much time until the same failure gets broadcasted.
 */
@Singleton
public class SimpleFailCondition implements FailCondition {

    //Each Tests starts its own injector, so each test brings up its own Broadcaster.
    //Could inject this in QOSCommonModule instead so there is only one Broadcaster, haven't tried
    //For now these has to be static.
    private static final Multiset<TestId> FAILURES = ConcurrentHashMultiset.create();
    private static final Map<TestId, Long> FIRST_FAILURE_TIME = Maps.newConcurrentMap();
    private static final Map<TestId, Integer> FAILURE_MULTIPLIER = Maps.newConcurrentMap();

    private final Integer reBroadcastFailureInMinutes;
    private final Provider<QOSServerConfiguration> configurationProvider;

    @Inject
    public SimpleFailCondition(
            /** Using a Provider since running from IDE it gets initalizated. **/
            Provider<QOSServerConfiguration> configurationProvider,
            @Named(QOSPropertiesModule.RE_BROADCAST_FAILURE_IN_MINUTES) String reBroadastFailureInMinutes) {
        this.configurationProvider = Preconditions.checkNotNull(configurationProvider);
        this.reBroadcastFailureInMinutes = Integer.valueOf(Preconditions.checkNotNull(reBroadastFailureInMinutes));

    }

    @Override
    public Broadcast failed(TestId testId) {
        Preconditions.checkNotNull(testId);
        Integer consecutiveFailures = configurationProvider.get().getTest().getConsecutiveFailures();
        FAILURES.add(testId);
        if (FAILURES.count(testId) == consecutiveFailures) {
            FIRST_FAILURE_TIME.put(testId, System.currentTimeMillis());
            return Broadcast.FIRST;
        }
        Long when = FIRST_FAILURE_TIME.get(testId);
        int mulitiplier = FAILURE_MULTIPLIER.getOrDefault(testId, 1);
        if (when != null && (when + mulitiplier * TimeUnit.MINUTES.toMillis(reBroadcastFailureInMinutes)) < System.currentTimeMillis()) {
            mulitiplier++;
            FAILURE_MULTIPLIER.put(testId, mulitiplier);
            return Broadcast.REBROADCAST;
        }
        return Broadcast.NO;
    }

    @Override
    public Broadcast success(TestId testId) {
        Preconditions.checkNotNull(testId);
        Integer consecutiveFailures = configurationProvider.get().getTest().getConsecutiveFailures();
        int count = FAILURES.count(testId);
        FIRST_FAILURE_TIME.remove(testId);
        FAILURE_MULTIPLIER.put(testId, 1);
        FAILURES.removeAll(Lists.newArrayList(testId));
        if (count >= consecutiveFailures) {
            return Broadcast.RECOVERY;
        } else {
            return Broadcast.NO;
        }
    }

    @Override
    public Long firstFailure(TestId testId) {
        return FIRST_FAILURE_TIME.get(testId);
    }

    @Override
    public void reset() {
        FAILURES.clear();
        FIRST_FAILURE_TIME.clear();
        FAILURE_MULTIPLIER.clear();
    }
}
