package io.split.testrunner;

import java.lang.reflect.Method;

/**
 * Guice Factory for creating test runners.
 */
@FunctionalInterface
public interface TestRunnerFactory {
    TestRunner create(Method test);
}
