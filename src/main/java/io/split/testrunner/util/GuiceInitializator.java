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
 *             If the test is being run from the IDE, QOSConfig will set the location of the properties file.
 *         </li>
 *     </ul>
 * </p>
 */
public class GuiceInitializator {
    private static Path propertiesPath;
    private static boolean qos = false;
    private static boolean method = false;

    public static boolean isQos() {
        return qos;
    }

    public static void setQos() {
        qos = true;
    }

    public static boolean isMethod() {
        return method;
    }

    public static void setMethod() {
        method = true;
    }

    public static void setPath(Path path) {
        Preconditions.checkNotNull(path);
        propertiesPath = path;
    }

    public static Path getPath() {
        Preconditions.checkNotNull(
                propertiesPath,
                "Properties Path not set");
        return propertiesPath;
    }
}
