package io.split.qos.server.modules;

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
 * Module for loading properties from the properties file.
 */
public class QOSPropertiesModule extends AbstractModule {
    // ------------------
    // COMMON PROPERTIES
    // ------------------

    // ------------------
    // SERVER PROPERTIES
    // ------------------

    // How many tests will be running in parallel
    // Default 10
    public static final String PARALLEL_TESTS = "PARALLEL_TESTS";
    // How much time to wait when shutdown is triggered.
    // Default 10 minutes
    public static final String SHUTDOWN_WAIT_IN_MINUTES = "SHUTDOWN_WAIT_IN_MINUTES";
    // How much time to wait between the test finishes and the same test is re run.
    // Default 5 minutes.
    public static final String DELAY_BETWEEN_IN_SECONDS = "DELAY_BETWEEN_IN_SECONDS";
    // How much time to wait until retry when the test fails
    // Default 1 minute.
    public static final String DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL = "DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL";
    // Whether to spread the tests accross the delay_between_in_seconds or start
    // all the tests when the servers starts.
    // Default true
    public static final String SPREAD_TESTS = "SPREAD_TESTS";
    // If true, there will be no cycle, failed tests will be rescheduled according to
    // DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL, but succeeded ones won't be rescheduled
    // Default false
    public static final String ONE_RUN = "ONE_RUN";
    // Which suites to run.
    // Default SMOKE
    public static final String SUITES = "SUITES";
    // Root package where the tests are going to be looked.
    // Default io.split
    public static final String SUITES_PACKAGE = "SUITES_PACKAGE";
    // For displaying a short description of the QOS in the Info Command
    // Default "-".
    public static final String DESCRIPTION = "DESCRIPTION";
    // For setting in which timezone to post the messages that are time related.
    // Default America/Los_Angeles
    public static final String TIME_ZONE = "TIME_ZONE";
    // Binding for the properties file with all the config.
    public static final String CONFIGURATION = "CONFIGURATION";

    // ------------------
    // TEST PROPERTIES
    // ------------------

    // Default 2
    public static final String CONSECUTIVE_FAILURES = "CONSECUTIVE_FAILURES";
    // If a test keeps failing, how often to re broadcast the failure
    // Default is 60 minutes
    public static final String RE_BROADCAST_FAILURE_IN_MINUTES = "RE_BROADCAST_FAILURE_IN_MINUTES";

    private static final Map<String, String> defaultProperties = Maps.newHashMap();

    static {

        // Server
        defaultProperties.put(PARALLEL_TESTS, "10");
        defaultProperties.put(SHUTDOWN_WAIT_IN_MINUTES, "10");
        defaultProperties.put(DELAY_BETWEEN_IN_SECONDS, "300");
        defaultProperties.put(DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL, "60");
        defaultProperties.put(SPREAD_TESTS, "true");
        defaultProperties.put(ONE_RUN, "false");
        defaultProperties.put(DESCRIPTION, "-");
        defaultProperties.put(SUITES, "SMOKE");
        defaultProperties.put(SUITES_PACKAGE, "io.split");
        defaultProperties.put(TIME_ZONE, "America/Los_Angeles");

        // Test
        defaultProperties.put(CONSECUTIVE_FAILURES, "2");
        defaultProperties.put(RE_BROADCAST_FAILURE_IN_MINUTES, "60");
    }

    public QOSPropertiesModule() { }

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

        bind(Properties.class).annotatedWith(Names.named(CONFIGURATION)).toInstance(theProperties);
    }
}
