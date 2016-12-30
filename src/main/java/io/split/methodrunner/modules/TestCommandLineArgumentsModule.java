package io.split.methodrunner.modules;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import io.split.methodrunner.commandline.MethodCommandLineArguments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Guice Module for Command Line Arguments
 */
public class TestCommandLineArgumentsModule extends AbstractModule {

    private final MethodCommandLineArguments methodCommandLineArguments;

    /**
     * Default Cosntructor.
     *
     * @param arguments the command line arguments.
     */
    public TestCommandLineArgumentsModule(String[] arguments) {
        methodCommandLineArguments = MethodCommandLineArguments.initialize(Preconditions.checkNotNull(arguments));
    }

    /**
     * @return the properties where the conf resides.
     */
    public List<Path> propertiesPath() {
        List<Path> result = Lists.newArrayList();
        for(String conf : methodCommandLineArguments.confs()) {
            if (!Files.exists(Paths.get(conf))) {
                throw new IllegalArgumentException(String.format("File %s does not exist", conf));
            }
            result.add(Paths.get(conf));
        }
        return result;
    }

    @Override
    protected void configure() {
        bind(MethodCommandLineArguments.class).toInstance(methodCommandLineArguments);
    }
}
