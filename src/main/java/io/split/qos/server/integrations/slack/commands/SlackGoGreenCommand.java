package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.QOSServerBehaviour;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.Util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Runs all the tests that have not been green since the specified time of the day.
 */
public class SlackGoGreenCommand implements SlackCommandExecutor {
    private static final String TITLE = "[%s] Go Green QOS Server";

    private final String serverName;
    private final QOSServerBehaviour behaviour;
    private final SlackCommonFormatter formatter;
    private final DateFormatter dateFormatter;
    private final SlackCommandGetter slackCommandGetter;

    @Inject
    public SlackGoGreenCommand(
            QOSServerBehaviour behaviour,
            SlackCommonFormatter formatter,
            DateFormatter dateFormatter,
            SlackCommandGetter slackCommandGetter,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.behaviour = behaviour;
        this.formatter = Preconditions.checkNotNull(formatter);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        Optional<SlackCommand> slackCommand = slackCommandGetter.get(messagePosted);
        if (!slackCommand.isPresent()) {
            slackEmpty(messagePosted, session);
            return false;
        }
        List<String> arguments = slackCommand.get().arguments();
        if (arguments == null || arguments.isEmpty()) {
            slackEmpty(messagePosted, session);
            return false;
        }
        if (arguments.get(0).equals(serverName)) {
            arguments.remove(0);
        }
        if (arguments.isEmpty()) {
            slackEmpty(messagePosted, session);
            return false;
        }
        if (arguments.size() != 1) {
            wrongParameter(messagePosted, session);
            return false;
        }
        try {
            HourMinutesSeconds hourMinutesSeconds = getTimeFrom(arguments.get(0));
            Long time = dateFormatter.getTodayAt(hourMinutesSeconds.hour, hourMinutesSeconds.minutes, hourMinutesSeconds.seconds);
            List<Method> tests = behaviour.runNotGreen(time);
            String title = String.format("[%s] RUNNING TESTS THAT HAVE NOT BEEN GREEN SINCE %s", serverName, dateFormatter.formatDate(time));
            String text = String.format("Running now %s tests triggered by %s", tests.size(), messagePosted.getSender().getUserName());

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment
                    .setColor("good");

            List<String> toBeRun = tests
                    .stream()
                    .map(value -> Util.id(value))
                    .collect(Collectors.toList());

            session
                    .sendMessage(
                            messagePosted.getChannel(),
                            "",
                            slackAttachment);

            formatter
                    .groupMessage(toBeRun)
                    .forEach(group -> session
                            .sendMessage(messagePosted.getChannel(),
                                    group));
        } catch (IllegalArgumentException e) {
            wrongParameter(messagePosted, session);
            return false;
        }
        return true;
    }

    @Override
    public String help() {
        return "gogreen [server-name] [HH:MM:SS]: Runs tests immediately that have not been green since that time today";
    }

    private void slackEmpty(SlackMessagePosted messagePosted, SlackSession session) {
        warningMessage("Go Green needs an HH:MM:SS to run", messagePosted, session);
    }

    private void wrongParameter(SlackMessagePosted messagePosted, SlackSession session) {
        warningMessage("Go Green needs only one parameter HH:MM:SS to run", messagePosted, session);
    }

    private void warningMessage(String message, SlackMessagePosted messagePosted, SlackSession session) {
        String title = String.format(TITLE, serverName);
        SlackAttachment slackAttachment = new SlackAttachment(title, "", message, null);
        slackAttachment
                .setColor("warning");
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
    }

    private HourMinutesSeconds getTimeFrom(String argument) throws IllegalArgumentException {
        if (Strings.isNullOrEmpty(argument)) {
            throw new IllegalArgumentException("argument cannot be empty");
        }
        String[] splitted = argument.split(":");
        if (splitted.length != 3) {
            throw new IllegalArgumentException("time should be HH:MM:SS");
        }
        try {
            return new HourMinutesSeconds(
                    Integer.valueOf(splitted[0]), Integer.valueOf(splitted[1]), Integer.valueOf(splitted[2]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("time should be HH:MM:SS");
        }
    }

    private static class HourMinutesSeconds {
        private final int hour;
        private final int minutes;
        private final int seconds;

        private HourMinutesSeconds(int hour, int minutes, int seconds) throws IllegalArgumentException {
            if (hour < 0 || hour > 23) {
                throw new IllegalArgumentException("Hour should be between 0 and 23");
            }
            this.hour = hour;
            if (minutes < 0 || minutes > 59) {
                throw new IllegalArgumentException("Minutes should be between 0 and 59");
            }
            this.minutes = minutes;
            if (seconds < 0 || seconds > 59) {
                throw new IllegalArgumentException("Seconds should be between 0 and 59");
            }
            this.seconds = seconds;
        }
    }
}
