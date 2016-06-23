package io.split.qos.server.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

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
public class QOSPropertiesFinderHack {
    private static String propertiesPath = "";

    public static void setPath(String path) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(path));
        propertiesPath = path;
    }

    public static String getPath() {
        return propertiesPath;
    }
}
