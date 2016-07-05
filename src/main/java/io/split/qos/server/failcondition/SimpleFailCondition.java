package io.split.qos.server.failcondition;

import com.google.common.base.Preconditions;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.testrunner.util.Util;
import org.junit.runner.Description;

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
    private static final Multiset<String> FAILURES = ConcurrentHashMultiset.create();
    private static final Map<String, Long> FIRST_FAILURE_TIME = Maps.newConcurrentMap();
    private static final Map<String, Integer> FAILURE_MULTIPLIER = Maps.newConcurrentMap();

    private final int consecutiveFailures;
    private final Integer reBroadcastFailureInMinutes;

    @Inject
    public SimpleFailCondition(
            @Named(QOSPropertiesModule.CONSECUTIVE_FAILURES) String consecutiveFailures,
            @Named(QOSPropertiesModule.RE_BROADCAST_FAILURE_IN_MINUTES) String reBroadastFailureInMinutes) {
        this.consecutiveFailures = Integer.valueOf(Preconditions.checkNotNull(consecutiveFailures));
        this.reBroadcastFailureInMinutes = Integer.valueOf(Preconditions.checkNotNull(reBroadastFailureInMinutes));

    }

    @Override
    public Broadcast failed(Description description) {
        FAILURES.add(Util.id(description));
        if (FAILURES.count(Util.id(description)) == consecutiveFailures) {
            FIRST_FAILURE_TIME.put(Util.id(description), System.currentTimeMillis());
            return Broadcast.FIRST;
        }
        Long when = FIRST_FAILURE_TIME.get(Util.id(description));
        int mulitiplier = FAILURE_MULTIPLIER.getOrDefault(Util.id(description), 1);
        if (when != null && (when + mulitiplier * TimeUnit.MINUTES.toMillis(reBroadcastFailureInMinutes)) < System.currentTimeMillis()) {
            mulitiplier++;
            FAILURE_MULTIPLIER.put(Util.id(description), mulitiplier);
            return Broadcast.REBROADCAST;
        }
        return Broadcast.NO;
    }

    @Override
    public Broadcast success(Description description) {
        int count = FAILURES.count(Util.id(description));
        FIRST_FAILURE_TIME.remove(Util.id(description));
        FAILURE_MULTIPLIER.put(Util.id(description), 1);
        FAILURES.removeAll(Lists.newArrayList(Util.id(description)));
        if (count >= consecutiveFailures) {
            return Broadcast.RECOVERY;
        } else {
            return Broadcast.NO;
        }
    }

    @Override
    public Long firstFailure(Description description) {
        return FIRST_FAILURE_TIME.get(Util.id(description));
    }
}
