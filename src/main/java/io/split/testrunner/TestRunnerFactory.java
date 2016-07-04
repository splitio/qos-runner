package io.split.testrunner;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Guice Factory for creating test runners.
 */
@FunctionalInterface
public interface TestRunnerFactory {
    TestRunner create(Method test, Optional<String> nameAppender);
}
