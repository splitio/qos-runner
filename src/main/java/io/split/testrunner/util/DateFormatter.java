package io.split.testrunner.util;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.server.modules.QOSPropertiesModule;

import java.text.SimpleDateFormat;
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
}
