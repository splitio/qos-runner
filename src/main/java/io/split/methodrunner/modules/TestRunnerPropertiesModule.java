package io.split.methodrunner.modules;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.split.testrunner.util.GuiceInitializator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;

public class TestRunnerPropertiesModule extends AbstractModule {
    public static final String TIMEOUT_IN_MINUTES = "TIMEOUT_IN_MINUTES";

    private static final Map<String, String> defaultProperties = Maps.newHashMap();

    static {
        // Common
        defaultProperties.put(TIMEOUT_IN_MINUTES, "20");
    };

    public TestRunnerPropertiesModule() {}

    @Override
    protected void configure() {
        // Loads the default properties.
        Properties theProperties = new Properties();
        defaultProperties.entrySet()
                .stream()
                .forEach(entry -> theProperties.setProperty(entry.getKey(), entry.getValue()));

        Path propertiesPath = GuiceInitializator.getPath();
        // Loads the properties set in the properties file.
        if (Files.exists(propertiesPath)) {
            try {
                theProperties.load(new FileInputStream(propertiesPath.toFile()));
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load properties file " + propertiesPath, e);
            }
        } else {
            throw new IllegalArgumentException("Properties file does not exist " + propertiesPath);
        }

        Names.bindProperties(binder(), theProperties);
    }
}
