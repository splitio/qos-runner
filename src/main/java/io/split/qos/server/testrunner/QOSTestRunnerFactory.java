package io.split.qos.server.testrunner;

import java.lang.reflect.Method;

/**
 * Guice Factory for creating test runners.
 */
@FunctionalInterface
public interface QOSTestRunnerFactory {
    QOSTestRunner create(Method test);
}
