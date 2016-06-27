package io.split.qos.server.failcondition;

import org.junit.runner.Description;

/**
 * Defines whether failures/successes should be broadcasted.
 *
 * <p>
 *     Notice that each tests has its own injector, meaning that each test
 *     will create its own instance, no matter if you define Singleton
 *     the implementation of this interface.
 * </p>
 */
public interface FailCondition {
    /**
     *  Given a failed test, defines whether the failure should be brodcasted.
     *
     * @param description the junit description of the failed test.
     * @return whether to broadcast the failure
     */
    Broadcast failed(Description description);

    /**
     * Given a succeeded test, defines whether it should broadcast or not the success.
     *
     * @param description the junit description of the succeeded test.
     * @return whether to broadcast the success.
     */
    Broadcast success(Description description);

    /**
     * @param description the junit description of the test.
     * @return when was the first failure found.
     */
    Long firstFailure(Description description);
}
