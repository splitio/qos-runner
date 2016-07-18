package io.split.testrunner.util;

import com.google.common.base.Preconditions;

import java.nio.file.Path;

/**
 * We need to inject the properties file both from the server, and from each test (each one has its own injector).
 *
 * Since we cannot use guice to store it (this is happening when Modules are being loaded), then we use this class with
 * static setters to keep from where to load the properties file.
 *
 * <p>
 *     <ul>
 *         <li>
 *             If the test is being run from the server, the server when it starts it defines where the properties file
 *             resides.
 *         </li>
 *         <li>
 *             If the test is being run from the IDE, PropertiesConfig will set the location of the properties file.
 *         </li>
 *         <li>
 *             If the test is being run from the testrunner, it is set there.
 *         </li>
 *     </ul>
 * </p>
 */
public class GuiceInitializator {
    private static Path propertiesPath;
    private static boolean qos = false;
    private static boolean method = false;
    private static boolean suite = false;

    /**
     * @return whether the test was started from QOS Server
     */
    public static boolean isQos() {
        return qos;
    }

    /**
     * Set the run to QOS Server.
     */
    public static void setQos() {
        qos = true;
    }

    /**
     * @return whether the test was started from TestMethodRunner
     */
    public static boolean isMethod() {
        return method;
    }

    /**
     * Set the run to TestMethodRunner.
     */
    public static void setMethod() {
        method = true;
    }

    /**
     * Set the run to TestSuiteRunner.
     */
    public static void setSuite() {
        suite = true;
    }

    /**
     * @return whether the test was started from TestSuiterunner
     */
    public static boolean isSuite() {
        return suite;
    }

    /**
     * Set where the conf file is
     *
     * @param path where the conf file is.
     */
    public static void setPath(Path path) {
        Preconditions.checkNotNull(path);
        propertiesPath = path;
    }

    /**
     * @return the path where the conf file is.
     */
    public static Path getPath() {
        return propertiesPath;
    }
}
