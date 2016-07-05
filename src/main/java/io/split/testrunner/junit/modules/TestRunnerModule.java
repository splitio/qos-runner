package io.split.testrunner.junit.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.split.testrunner.junit.JUnitRunner;
import io.split.testrunner.junit.JUnitRunnerFactory;

/**
 * Guice Module for the test runner.
 */
public class TestRunnerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(JUnitRunner.class, JUnitRunner.class)
                .build(JUnitRunnerFactory.class));
    }
}
