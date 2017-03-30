package io.split.qos.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.TestResult;
import io.split.qos.server.util.TestId;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Keeps track of the tests that are running.
 */
@Singleton
public class QOSTestsTracker {

    private final Map<TestId, Tracked> tracked;

    @Inject
    public QOSTestsTracker() {
        tracked = Maps.newConcurrentMap();
    }

    public synchronized void track(Method method, JUnitRunner runner, ListenableFuture<TestResult> future) {
        this.tracked.put(TestId.fromMethod(method), new Tracked(method, runner, future));
    }

    public synchronized Map<TestId, Tracked> tests() {
        return tracked;
    }

    public Map<TestId, Tracked> tests(String fuzzyClassOrName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyClassOrName));
        return Maps.filterKeys(tracked, input -> input.contains(fuzzyClassOrName));
    }

    public Map<TestId, Tracked> tests(String fuzzyClass, String fuzzyName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyClass));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyName));
        return Maps.filterKeys(tracked, input -> input.contains(fuzzyClass, fuzzyName));
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
