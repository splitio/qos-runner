package io.split.methodrunner.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.split.methodrunner.commandline.MethodCommandLineArguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Guice Module for Comand Line Arguments
 */
public class CommandLineArgumentsModule extends AbstractModule {

    private final MethodCommandLineArguments methodCommandLineArguments;

    /**
     * Default Cosntructor.
     *
     * @param arguments the command line arguments.
     */
    public CommandLineArgumentsModule(String[] arguments) {
        methodCommandLineArguments = MethodCommandLineArguments.initialize(Preconditions.checkNotNull(arguments));
    }

    /**
     * @return the properties where the conf resides.
     */
    public Path propertiesPath() {
        Path path = Paths.get(methodCommandLineArguments.conf());
        if (Files.exists(path)) {
            return path;
        } else {
            throw new IllegalArgumentException(String.format("File %s does not exists", methodCommandLineArguments.conf()));
        }
    }

    @Override
    protected void configure() {
        bind(MethodCommandLineArguments.class).toInstance(methodCommandLineArguments);
    }
}
