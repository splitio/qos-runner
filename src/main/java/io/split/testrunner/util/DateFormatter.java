package io.split.testrunner.util;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.server.modules.QOSPropertiesModule;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

@Singleton
public class DateFormatter {

    private final String timeZone;

    @Inject
    public DateFormatter(
            @Named(QOSPropertiesModule.TIME_ZONE) String timeZone) {
        this.timeZone = Preconditions.checkNotNull(timeZone);
    }

    public String formatDate(Long date) {
        if (date != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss z");
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            return dateFormat.format(date);
        } else {
            return "--";
        }
    }

    public String formatHour(Long hour) {
        if (hour != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
            return dateFormat.format(hour);
        } else {
            return "--";
        }
    }

    /**
     * Given an hour (0-23), minutes (0-59) and seconds (0-59) it will return
     * a long representation of today at that time
     *
     * @param hour between 0 and 23
     * @param minutes between 0 and 59
     * @param seconds between 0 and 60
     */
    public Long getTodayAt(int hour, int minutes, int seconds) throws IllegalArgumentException {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour shoud be between 0 and 23");
        }
        if (minutes < 0 || minutes > 59) {
            throw new IllegalArgumentException("Minutes shoud be between 0 and 59");
        }
        if (seconds < 0 || seconds > 59) {
            throw new IllegalArgumentException("Seconds shoud be between 0 and 59");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(timeZone));
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minutes);
        cal.set(Calendar.SECOND, seconds);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
}
