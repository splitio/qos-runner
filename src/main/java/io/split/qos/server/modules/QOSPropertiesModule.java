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

    private static final String ENV_VAR_PREFIX = "QOS_PROP_";

    // ------------------
    // COMMON PROPERTIES
    // ------------------
    public static final String SDK = "SDK";
    public static final String TREATMENT_URL = "TREATMENT_URL";
    public static final String TREATMENT_CONFIG_URL = "TREATMENT_CONFIG_URL";
    public static final String CONFIG_URL= "CONFIG_URL";
    public static final String GET_TREATMENTS_URL = "GET_TREATMENTS_URL";
    public static final String GET_TREATMENTS_CONFIG_URL = "GET_TREATMENTS_CONFIG_URL";
    public static final String SPLIT_URL = "SPLIT_URL";
    public static final String SPLITS_URL = "SPLITS_URL";
    public static final String SPLITNAMES_URL = "SPLITNAMES_URL";
    public static final String WAIT_TIME_IN_SECONDS = "WAIT_TIME_IN_SECONDS";
    public static final String WAIT_BETWEEN_ITERATIONS_IN_MILLISECONDS = "WAIT_BETWEEN_ITERATIONS_IN_MILLISECONDS";
    public static final String URL = "URL";
    public static final String USER_EMAIL = "USER_EMAIL";
    public static final String USER_PASSWORD = "USER_PASSWORD";
    public static final String ORG_ID = "ORG_ID";
    public static final String ENVIRONMENT_NAME = "ENVIRONMENT_NAME";
    public static final String USER_IN_EMPLOYEES = "USER_IN_EMPLOYEES";
    public static final String TRAFFIC_NAME = "TRAFFIC_NAME";
    public static final String WEB_API_URL = "WEB_API_URL";
    public static final String SDK_API_URL = "SDK_API_URL";
    public static final String VERSION_URL = "VERSION_URL";
    public static final String EXPECT_IMPRESSIONS = "EXPECT_IMPRESSIONS";
    public static final String EXPECT_LISTENER_IMPRESSIONS = "EXPECT_LISTENER_IMPRESSIONS";
    public static final String IMPRESSIONS_URL = "IMPRESSIONS_URL";
    public static final String CAPABILITIES = "CAPABILITIES";
    public static final String LIVE_TAIL_URL = "LIVE_TAIL_URL";
    public static final String CALL_HOME_URL = "CALL_HOME_URL";
    public static final String CALL_HOME_AUTH = "CALL_HOME_AUTH";
    public static final String GET_TREATMENTS_BY_FLAG_SET = "GET_TREATMENTS_BY_FLAG_SET";
    public static final String GET_TREATMENTS_BY_FLAG_SETS = "GET_TREATMENTS_BY_FLAG_SETS";
    
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

    // If a test keeps failing, how often to re broadcast the failure
    // Default is 60 minutes
    public static final String RE_BROADCAST_FAILURE_IN_MINUTES = "RE_BROADCAST_FAILURE_IN_MINUTES";

    private static final Map<String, String> defaultProperties = Maps.newHashMap();

    public static final String CHECK_LABEL_IMPRESSIONS = "CHECK_LABEL_IMPRESSIONS";
    public static final String CHECK_MACHINE_IP_IMPRESSIONS = "CHECK_MACHINE_IP_IMPRESSIONS";
    public static final String CHECK_MACHINE_NAME_IMPRESSIONS =  "CHECK_MACHINE_NAME_IMPRESSIONS";
    public static final String CHECK_CHANGE_NUMBER =  "CHECK_CHANGE_NUMBER";
    public static final String CHECK_SDK_IMPRESSIONS = "CHECK_SDK_IMPRESSIONS";

    static {
        //Common
        defaultProperties.put(URL, "");
        defaultProperties.put(WAIT_TIME_IN_SECONDS, "90");
        defaultProperties.put(WAIT_BETWEEN_ITERATIONS_IN_MILLISECONDS, "500");
        defaultProperties.put(USER_EMAIL, "");
        defaultProperties.put(USER_PASSWORD, "");
        defaultProperties.put(ORG_ID, "");
        defaultProperties.put(ENVIRONMENT_NAME, "");
        defaultProperties.put(TRAFFIC_NAME, "");
        defaultProperties.put(WEB_API_URL, "");
        defaultProperties.put(SDK_API_URL, "");
        defaultProperties.put(USER_IN_EMPLOYEES, "");
        defaultProperties.put(EXPECT_IMPRESSIONS, "false");
        defaultProperties.put(EXPECT_LISTENER_IMPRESSIONS, "false");
        defaultProperties.put(IMPRESSIONS_URL, "impressions");
        defaultProperties.put(VERSION_URL, "version");

        defaultProperties.put(USER_IN_EMPLOYEES, "user_for_testing_do_no_erase");
        defaultProperties.put(SDK, "JAVA");
        defaultProperties.put(TREATMENT_URL, "automation");
        defaultProperties.put(GET_TREATMENTS_URL, "gettreatments");
        defaultProperties.put(GET_TREATMENTS_CONFIG_URL, "gettreatmentswithconfig");
        defaultProperties.put(CONFIG_URL, "config");
        defaultProperties.put(SPLIT_URL, "manager/split");
        defaultProperties.put(SPLITS_URL, "manager/splits");
        defaultProperties.put(SPLITNAMES_URL, "manager/splitnames");
        defaultProperties.put(IMPRESSIONS_URL, "impressions");
        defaultProperties.put(CAPABILITIES, "capabilities/chrome.yaml");

        defaultProperties.put(CHECK_LABEL_IMPRESSIONS, "false");
        defaultProperties.put(CHECK_MACHINE_IP_IMPRESSIONS, "false");
        defaultProperties.put(CHECK_MACHINE_NAME_IMPRESSIONS , "false");
        defaultProperties.put(CHECK_CHANGE_NUMBER, "false");
        defaultProperties.put(CHECK_SDK_IMPRESSIONS, "false");
        defaultProperties.put(LIVE_TAIL_URL, "");
        defaultProperties.put(CALL_HOME_URL, "");
        defaultProperties.put(CALL_HOME_AUTH, "");
        defaultProperties.put(GET_TREATMENTS_BY_FLAG_SET, "gettreatmentsbyflagset");
        defaultProperties.put(GET_TREATMENTS_BY_FLAG_SETS, "gettreatmentsbyflagsets");
        
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
        defaultProperties.put(RE_BROADCAST_FAILURE_IN_MINUTES, "60");
    }

    public QOSPropertiesModule() {

    }

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

        System.getenv().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(ENV_VAR_PREFIX))
                .forEach(entry -> theProperties.put(entry.getKey().replace(ENV_VAR_PREFIX, ""), entry.getValue()));

        Names.bindProperties(binder(), theProperties);
        bind(Properties.class).annotatedWith(Names.named(CONFIGURATION)).toInstance(theProperties);
    }
}
