package io.split.qos.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.qos.server.failcondition.FailCondition;
import io.split.qos.server.util.TestId;
import org.joda.time.DateTime;
import org.junit.runner.Description;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;

/**
 * Maintains the state of the server that is shared accross the app.
 *
 * <p>
 *     HOW LAST GREEN IS CALCULATED:
 *
 *     The idea is to know when was the last time all the tests were green, so you can compare it with when you deployed.
 *     if lastGreen > whenYouDeployed, then means that all your tests have passed since you deployed and you are good to go.
 *
 *     Roughly the algorithm is:
 *     <ul>
 *         <li> succeededInARow is a Map indexed by the testId and has as value when it succeeded.</li>
 *         <li> if a test fails, then lastGreen is reset and succeededInARow is cleared</li>
 *         <li> if a test succeeded, it adds itself to the succeededInARow map.
 *         <li>
 *             if totalTests == succeededInARow.size(), means all the tests have passed. To find when was it green
 *             we check the earliest of all the succeeded tests.
 *         </li>
 *     </ul>
 * </p>
 */
@Singleton
public class QOSServerState {

    private final FailCondition failCondition;

    private Long lastGreen;

    private Status status;

    private Long activeSince;

    private Long lastTestFinished;

    private Long pausedSince;

    private Map<TestId, Long> succeededInARow;

    private Map<TestId, TestStatus> tests;

    private String who;

    @Inject
    public QOSServerState(FailCondition failCondition) {
        this.status = Status.PAUSED;
        this.activeSince = null;
        this.pausedSince = DateTime.now().getMillis();
        this.lastTestFinished = null;
        this.lastGreen = null;
        this.succeededInARow = Maps.newConcurrentMap();
        this.tests = Maps.newConcurrentMap();
        this.failCondition = failCondition;
    }

    public void resume(String who) {
        this.status = Status.ACTIVE;
        this.who = who;
        this.activeSince = DateTime.now().getMillis();
        this.pausedSince = null;
    }

    public void pause(String who) {
        this.status = Status.PAUSED;
        this.who = who;
        this.pausedSince = DateTime.now().getMillis();
        this.activeSince = null;
    }

    public void registerTest(Description description) {
        this.tests.put(TestId.fromDescription(description), TestStatus.get(null, null));
    }

    public void registerTest(Method method) {
        this.tests.put(TestId.fromMethod(method), TestStatus.get(null, null));
    }

    public void testSucceeded(TestId testId) {
        this.lastTestFinished = DateTime.now().getMillis();
        this.tests.put(testId, TestStatus.get(lastTestFinished, true));
        this.succeededInARow.put(testId, lastTestFinished);
        if (succeededInARow.size() == tests.size()) {
            lastGreen = Collections.min(succeededInARow.values());
        }
    }

    public void testSucceeded(Description description) {
        testSucceeded(TestId.fromDescription(description));;
    }

    public void testFailed(Description description) {
        testFailed(TestId.fromDescription(description));
    }

    public void testFailed(TestId testId) {
        this.lastTestFinished = DateTime.now().getMillis();
        this.tests.put(testId, TestStatus.get(lastTestFinished, false));
        this.succeededInARow.clear();
        this.lastGreen = null;
    }

    public void testAborted(Description description) {
        testAborted(TestId.fromDescription(description));
    }

    // Get the test back to the running queue when aborted
    public void testAborted(TestId testId) {
        this.tests.put(testId, TestStatus.empty());
    }

    public enum Status {
        ACTIVE,
        PAUSED,
        ;
    }

    public void reset() {
        this.failCondition.reset();
        this.lastGreen = null;
        this.succeededInARow.clear();
        for(TestId testId : this.tests.keySet()) {
            this.tests.put(testId, TestStatus.empty());
        }
    }

    public Status status() {
        return status;
    }

    public Map<TestId, TestStatus> tests() {
        return tests;
    }

    public Map<TestId, TestStatus> tests(String fuzzyClassOrName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyClassOrName));
        return Maps.filterKeys(tests, input -> input.contains(fuzzyClassOrName));
    }

    public Map<TestId, TestStatus> tests(String fuzzyClass, String fuzzyName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyClass));
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyName));
        return Maps.filterKeys(tests, input -> input.contains(fuzzyClass, fuzzyName));
    }

    public TestStatus test(Method method) {
        return test(TestId.fromMethod(Preconditions.checkNotNull(method)));
    }

    public TestStatus test(Description description) {
        return test(TestId.fromDescription(Preconditions.checkNotNull(description)));
    }

    private TestStatus test(TestId id) {
        return tests.get(Preconditions.checkNotNull(id));
    }

    public Map<TestId, TestStatus> failedTests() {
        return Maps.filterValues(tests, input -> input.succeeded() != null && !input.succeeded());
    }

    public Map<TestId, TestStatus> missingTests() {
        return Maps.filterValues(tests, input -> input.succeeded() == null);
    }

    public Map<TestId, TestStatus> succeededTests() {
        return Maps.filterValues(tests, input -> input.succeeded() != null && input.succeeded());
    }


    public boolean isActive() {
        return Status.ACTIVE.equals(status());
    }

    public boolean isPaused() {
        return Status.PAUSED.equals(status());
    }

    public String who() {
        return who;
    }

    public Long activeSince() {
        return activeSince;
    }

    public Long pausedSince() {
        return pausedSince;
    }

    public Long lastTestFinished() {
        return lastTestFinished;
    }

    public Long lastGreen() {
        return lastGreen;
    }

    public static class TestStatus {
        private final Long when;
        private final Boolean succeeded;

        private TestStatus(Long when, Boolean succeeded) {
            this.when = when;
            this.succeeded = succeeded;
        }

        public static TestStatus get(Long when, Boolean succeeded) {
            return new TestStatus(when, succeeded);
        }

        public static TestStatus empty() {
            return new TestStatus(null, null);
        }

        public Long when() {
            return when;
        }

        public boolean hasFinished() {
            return when() != null;
        }

        public Boolean succeeded() {
            return succeeded;
        }
    }
}
