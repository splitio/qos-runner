package io.split.suiterunner.modules;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.split.testrunner.util.GuiceInitializator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Guice Module for parsing Properties file from the configuration file.
 */
public class SuiteRunnerPropertiesModule extends AbstractModule {
    public static final String TIMEOUT_IN_MINUTES = "TIMEOUT_IN_MINUTES";

    private static final Map<String, String> defaultProperties = Maps.newHashMap();

    static {
        // Common
        defaultProperties.put(TIMEOUT_IN_MINUTES, "20");
    };

    public SuiteRunnerPropertiesModule() { }

    @Override
    protected void configure() {
        // Loads the default properties.
        Properties theProperties = new Properties();
        defaultProperties.entrySet()
                .stream()
                .forEach(entry -> theProperties.setProperty(entry.getKey(), entry.getValue()));

        List<Path> paths = GuiceInitializator.getPaths();
        for(Path propertiesPath : paths) {
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
        }
        Names.bindProperties(binder(), theProperties);
    }
}
