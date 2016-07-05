package io.split.testrunner.junit;

import io.split.testrunner.junit.JUnitRunner;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Guice Factory for creating test runners.
 */
@FunctionalInterface
public interface JUnitRunnerFactory {
    /**
     * Creates a JUnitRunner
     *
     * @param test the method to run
     * @param nameAppender a name appender in case it exists.
     * @return the JUnitRunner
     */
    JUnitRunner create(Method test, Optional<String> nameAppender);
}
