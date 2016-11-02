package io.split.testrunner.util;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class SlackColors {

    @Inject
    public SlackColors() {}

    public String getSuccess() {
        return "good";
    }

    public String getFailed() {
        return "danger";
    }

    public String getWarning() {
        return "warning";
    }

    public String getInfo() {
        return "#4747B2";
    }
}
