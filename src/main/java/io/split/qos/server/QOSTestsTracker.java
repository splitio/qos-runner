package io.split.qos.server;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.TestResult;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public synchronized void track(Method method, JUnitRunner runner, ListenableFuture<TestResult> future) {
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

    public synchronized List<Tracked> getTests(Optional<String> fuzzyClass, String fuzzyName) {
        return getAll()
                .stream()
                .filter(tracked -> {
                    if (fuzzyClass.isPresent() &&
                            (!tracked.method().getDeclaringClass().getName().toLowerCase().contains(fuzzyClass.get().toLowerCase()))) {
                        return false;
                    }
                    return tracked.method().getName().toLowerCase().contains(fuzzyName.toLowerCase());
                })
                .collect(Collectors.toList());
    }

    public static class Tracked {
        private final Method method;
        private final JUnitRunner runner;
        private final ListenableFuture<TestResult> future;

        private Tracked(Method method, JUnitRunner runner, ListenableFuture<TestResult> future) {
            this.method = Preconditions.checkNotNull(method);
            this.runner = Preconditions.checkNotNull(runner);
            this.future = Preconditions.checkNotNull(future);
        }

        public Method method() {
            return method;
        }

        public JUnitRunner runner() {
            return runner;
        }

        public ListenableFuture<TestResult> future() {
            return future;
        }

    }
}
