package io.split.methodrunner.modules;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class TestRunnerPropertiesModule extends AbstractModule {
    public static final String TIMEOUT_IN_MINUTES = "TIMEOUT_IN_MINUTES";

    private static final Map<String, String> defaultProperties = Maps.newHashMap();

    static {
        // Common
        defaultProperties.put(TIMEOUT_IN_MINUTES, "20");
    };

    private final Optional<Path> path;

    public TestRunnerPropertiesModule(Optional<Path> path) {
        this.path = Preconditions.checkNotNull(path);
    }

    @Override
    protected void configure() {
        // Loads the default properties.
        Properties theProperties = new Properties();
        defaultProperties.entrySet()
                .stream()
                .forEach(entry -> theProperties.setProperty(entry.getKey(), entry.getValue()));

        path.ifPresent(thePath -> {
            if (Files.exists(thePath)) {
                try {
                    theProperties.load(new FileInputStream(thePath.toFile()));
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to load properties file " + thePath, e);
                }
            } else {
                throw new IllegalArgumentException("Properties file does not exist " + thePath);
            }
        });
        Names.bindProperties(binder(), theProperties);
    }
}
