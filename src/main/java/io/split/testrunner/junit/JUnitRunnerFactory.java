package io.split.testrunner.junit;

import io.split.testrunner.junit.JUnitRunner;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Guice Factory for creating test runners.
 */
@FunctionalInterface
public interface JUnitRunnerFactory {
    JUnitRunner create(Method test, Optional<String> nameAppender);
}
