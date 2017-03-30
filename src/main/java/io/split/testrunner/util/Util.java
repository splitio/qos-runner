package io.split.testrunner.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class Util {
    private static final SecureRandom random = new SecureRandom();

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

    /**
     * Gets a random number between min and max.
     *
     * @param min the minimum number.
     * @param max the maximum number
     * @return the value between min and max.
     */
    public static long getRandom(int min, int max) {
        return (long)(random.nextInt(max - min + 1) + min);
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
