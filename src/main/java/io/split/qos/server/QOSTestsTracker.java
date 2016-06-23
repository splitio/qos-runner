package io.split.qos.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.testrunner.QOSTestResult;
import io.split.qos.server.testrunner.QOSTestRunner;
import io.split.qos.server.util.Util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Keeps track of the tests that are running.
 */
@Singleton
public class QOSTestsTracker {

    private final Map<String, Tracked> tracked;

    @Inject
    public QOSTestsTracker() {
        tracked = Maps.newConcurrentMap();
    }

    public synchronized void track(Method method, QOSTestRunner runner, ListenableFuture<QOSTestResult> future) {
        this.tracked.put(Util.id(method), new Tracked(method, runner, future));
    }

    public synchronized List<Tracked> getNotRunning() {
        return tracked
                .values()
                .stream()
                .filter(track -> !track.runner.isRunning())
                .collect(Collectors.toList());
    }

    public synchronized List<Tracked> getAll() {
        return Lists.newArrayList(tracked
                .values());
    }

    public static class Tracked {
        private final Method method;
        private final QOSTestRunner runner;
        private final ListenableFuture<QOSTestResult> future;

        private Tracked(Method method, QOSTestRunner runner, ListenableFuture<QOSTestResult> future) {
            this.method = Preconditions.checkNotNull(method);
            this.runner = Preconditions.checkNotNull(runner);
            this.future = Preconditions.checkNotNull(future);
        }

        public Method method() {
            return method;
        }

        public QOSTestRunner runner() {
            return runner;
        }

        public ListenableFuture<QOSTestResult> future() {
            return future;
        }

    }
}
