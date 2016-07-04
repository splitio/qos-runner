package io.split.testrunner.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import org.junit.runner.Description;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * Created on 6/1/16.
 */
public class Util {
    public static final Function<Long, String> TO_PRETTY_FORMAT = millis -> String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(millis),
            TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
            TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

    public static String shortenClass(Class<?> clazz) {
        Preconditions.checkNotNull(clazz);

        String[] subpackages = clazz.getCanonicalName().split("\\.");
        StringBuilder builder = new StringBuilder();
        for(int index = 0; index < subpackages.length -1; index++) {
            builder.append(subpackages[index].substring(0, 1) + ".");
        }
        return builder.append(subpackages[subpackages.length - 1]).toString();
    }

    public static String id(Description description) {
        return String.format("%s#%s", Util.shortenClass(description.getTestClass()), description.getMethodName());
    }

    public static String id(Method method) {
        return String.format("%s#%s", Util.shortenClass(method.getDeclaringClass()), method.getName());
    }

    /**
     * Waits for some time, handling Interrupted Exceptions.
     *
     * @param millis How much time to wait.
     */
    public static void pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("InterrupedException while pausing", e);
        }
    }
}
