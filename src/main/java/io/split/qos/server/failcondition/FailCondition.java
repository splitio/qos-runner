package io.split.qos.server.failcondition;

import io.split.qos.server.util.TestId;

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
     * @return whether to broadcast the failure
     */
    Broadcast failed(TestId testId);

    /**
     * Given a succeeded test, defines whether it should broadcast or not the success.
     *
     * @return whether to broadcast the success.
     */
    Broadcast success(TestId testId);

    /**
     * @return when was the first failure found.
     */
    Long firstFailure(TestId testId);
}
