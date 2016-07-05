package io.split.suiterunner.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.split.suiterunner.commandline.SuiteCommandLineArguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Guice Module for Command Line Arguments
 */
public class SuiteCommandLineArgumentsModule extends AbstractModule {
    private final SuiteCommandLineArguments suiteCommandLineArguments;

    /**
     * Default Cosntructor.
     *
     * @param arguments the command line arguments.
     */
    public SuiteCommandLineArgumentsModule(String[] arguments) {
        suiteCommandLineArguments = SuiteCommandLineArguments.initialize(Preconditions.checkNotNull(arguments));
    }

    /**
     * @return the properties where the conf resides.
     */
    public Path propertiesPath() {
        Path path = Paths.get(suiteCommandLineArguments.conf());
        if (Files.exists(path)) {
            return path;
        } else {
            throw new IllegalArgumentException(String.format("File %s does not exists", suiteCommandLineArguments.conf()));
        }
    }

    @Override
    protected void configure() {
        bind(SuiteCommandLineArguments.class).toInstance(suiteCommandLineArguments);
    }
}
