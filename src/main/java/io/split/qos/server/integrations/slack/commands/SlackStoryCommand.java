package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.stories.QOSStories;
import io.split.qos.server.stories.Story;
import io.split.qos.server.util.SlackMessageSender;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.Optional;

public class SlackStoryCommand extends SlackAbstractCommand {
    private final QOSStories stories;
    private final DateFormatter dateFormatter;

    @Inject
    public SlackStoryCommand(
            SlackColors slackColors,
            QOSStories stories,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandGetter slackCommandGetter,
            SlackMessageSender slackMessageSender,
            DateFormatter dateFormatter) {
        super(slackColors, serverName, slackMessageSender, slackCommandGetter);
        this.stories = Preconditions.checkNotNull(stories);
        this.dateFormatter = Preconditions.checkNotNull(dateFormatter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = command(messagePosted);
        List<String> arguments = slackCommand.arguments();
        Optional<Story> theStory;
        if (arguments.isEmpty()) {
            theStory = stories.getLatestFailedStory();
        } else if (arguments.size() == 1) {
            theStory = stories.getStory(Optional.empty(), arguments.get(0));
        } else {
            theStory = stories.getStory(Optional.of(arguments.get(0)), arguments.get(1));
        }
        if (theStory.isPresent()) {
            slackStory(theStory.get(), messagePosted, session);
            return true;
        }
        String text = "";
        if (arguments.isEmpty()) {
            text = "Could not find latest failed story";
        } else {
            if (arguments.size() == 1) {
                text = "Could not find story for test with name " + arguments.get(0);
            } else {
                text = String.format("Could not find story for test of class %s with name %s",
                        arguments.get(0),
                        arguments.get(1));
            }
        }
        messageSender()
                .sendWarning(slackCommand.command(),
                        text,
                        messagePosted.getChannel(),
                        session);
        return false;

    }

    private void slackStory(Story story, SlackMessagePosted messagePosted, SlackSession session) {
        List<SlackAttachment> toBeAdded = Lists.newArrayList();
        SlackCommand slackCommand = command(messagePosted);

        String color = story.isSucceeded() ? colors().getSuccess() : colors().getFailed();
        /** Story Test Id**/
        if (story.testId().isPresent()) {
            SlackAttachment slackAttachment = new SlackAttachment("Test: ", "", story.testId().get().toString(), null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);
        }
        /** Story Title **/
        if (story.title().isPresent()) {
            SlackAttachment slackAttachment = new SlackAttachment("Title: ", "", story.title().get(), null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);
        }
        /** Description **/
        if (story.description().isPresent()) {
            SlackAttachment slackAttachment = new SlackAttachment("Description: ", "", story.description().get(), null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);
        }
        /** Time **/
        if (story.started().isPresent()) {
            String started = dateFormatter.formatDate(story.started().get());
            String duration = (story.finished().isPresent())
                    ? dateFormatter.formatHour(story.finished().get() - story.started().get())
                    : "N/A";
            String time = String.format("Test started: %s, duration %s",
                    started,
                    duration);
            SlackAttachment slackAttachment = new SlackAttachment("Time: ", "", time, null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);
        }

        /** Steps **/
        if (!story.steps().isEmpty()) {
            String theStepTitle = "------------------------------ STEPS ------------------------------";
            SlackAttachment slackAttachment = new SlackAttachment(theStepTitle, "", "", null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);

            /** Each Step **/
            story
                    .steps()
                    .stream()
                    .forEach(step -> {
                        String title = String.format("%s [%s]", step.title(), dateFormatter.formatDate(step.started()));
                        SlackAttachment stepAttachment = new SlackAttachment(
                                title,
                                "",
                                String.join("\n", step.descriptions()),
                                null);
                        stepAttachment.setColor(colors().getWarning());
                        toBeAdded.add(stepAttachment);
                    });
        } else {
            SlackAttachment slackAttachment = new SlackAttachment("NO STEPS", "", "No @steps associated", null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);
        }
        messageSender().sendPartition(slackCommand.command(), session, messagePosted.getChannel(), toBeAdded);
    }

    @Override
    public String description() {
        return "Gets the story for the test. With no arguments will return last failed story";
    }

    @Override
    public String arguments() {
        return "[server-name (optional)] story [class name (optional)] [test name (optional)]";
    }

    @Override
    public boolean acceptsArguments(List<String> arguments) {
        Preconditions.checkNotNull(arguments);
        return arguments.size() == 1 || arguments.size() == 2;
    }
}
