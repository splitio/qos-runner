package io.split.methodrunner.modules;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.split.testrunner.TestRunner;
import io.split.testrunner.TestRunnerFactory;

public class TestRunnerModule extends AbstractModule {
    @Override
    protected void configure() {
        install(new FactoryModuleBuilder()
                .implement(TestRunner.class, TestRunner.class)
                .build(TestRunnerFactory.class));
    }
}
