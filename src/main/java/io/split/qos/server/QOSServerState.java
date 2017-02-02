package io.split.qos.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.split.testrunner.util.Util;
import org.joda.time.DateTime;
import org.junit.runner.Description;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @JsonProperty
    private Long lastGreen;

    @JsonProperty
    private Status status;

    @JsonProperty
    private Long activeSince;

    @JsonProperty
    private Long lastTestFinished;

    @JsonProperty
    private Long pausedSince;

    @JsonProperty
    private Map<String, Long> succeededInARow;

    @JsonProperty
    private Map<String, TestStatus> tests;
    private String who;

    @Inject
    public QOSServerState() {
        this.status = Status.PAUSED;
        this.activeSince = null;
        this.pausedSince = DateTime.now().getMillis();
        this.lastTestFinished = null;
        this.lastGreen = null;
        this.succeededInARow = Maps.newConcurrentMap();
        this.tests = Maps.newConcurrentMap();
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
        this.tests.put(Util.id(description), TestStatus.get(null, null));
    }

    public void registerTest(Method method) {
        this.tests.put(Util.id(method), TestStatus.get(null, null));
    }

    public void testSucceeded(String testId) {
        this.lastTestFinished = DateTime.now().getMillis();
        this.tests.put(testId, TestStatus.get(lastTestFinished, true));
        this.succeededInARow.put(testId, lastTestFinished);
        if (succeededInARow.size() == tests.size()) {
            lastGreen = Collections.min(succeededInARow.values());
        }
    }

    public void testSucceeded(Description description) {
        testSucceeded(Util.id(description));;
    }

    public void testFailed(Description description) {
        testFailed(Util.id(description));
    }

    public void testFailed(String testId) {
        this.lastTestFinished = DateTime.now().getMillis();
        this.tests.put(testId, TestStatus.get(lastTestFinished, false));
        this.succeededInARow.clear();
        this.lastGreen = null;
    }

    public enum Status {
        ACTIVE,
        PAUSED,
        ;
    }

    public Status status() {
        return status;
    }

    public Map<String, TestStatus> tests() {
        return tests;
    }

    public List<TestFailed> failedTests() {
        return tests
                .entrySet()
                .stream()
                .filter(entry -> (entry.getValue().succeeded() != null && !entry.getValue().succeeded()))
                .sorted((o1, o2) -> o1.getValue().when().compareTo(o2.getValue().when()))
                .map(entry -> new TestFailed(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
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

    public static class TestFailed {
        private final String name;
        private final TestStatus status;

        private TestFailed(String name, TestStatus status) {
            this.name = name;
            this.status = status;
        }

        public String name() { return name; }

        public TestStatus status() {
            return status;
        }
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
