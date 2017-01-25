package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommand;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandGetter;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.stories.QOSStories;
import io.split.qos.server.stories.Story;
import io.split.testrunner.util.DateFormatter;
import io.split.testrunner.util.SlackColors;

import java.util.List;
import java.util.Optional;

public class SlackStoryCommand implements SlackCommandExecutor {
    private static final String TITLE = "[%s] STORY";

    private static final int CHUNK_SIZE = 50;

    private final SlackColors slackColors;
    private final QOSStories stories;
    private final String serverName;
    private final SlackCommandGetter slackCommandGetter;
    private final DateFormatter dateFormater;

    @Inject
    public SlackStoryCommand(
            SlackColors slackColors,
            QOSStories stories,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            SlackCommandGetter slackCommandGetter,
            DateFormatter dateFormatter) {
        this.slackColors = Preconditions.checkNotNull(slackColors);
        this.stories = Preconditions.checkNotNull(stories);
        this.serverName = Preconditions.checkNotNull(serverName);
        this.slackCommandGetter = Preconditions.checkNotNull(slackCommandGetter);
        this.dateFormater = Preconditions.checkNotNull(dateFormatter);
    }


    @Override
    public boolean test(SlackMessagePosted messagePosted, SlackSession session) {
        SlackCommand slackCommand = slackCommandGetter.get(messagePosted).get();
        List<String> arguments = slackCommand.arguments();
        Optional<Story> theStory;
        if (!arguments.isEmpty() && arguments.get(0).equals(serverName)) {
            arguments.remove(0);
        }
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
        slackEmpty(messagePosted, session, arguments);
        return false;

    }

    private void slackStory(Story story, SlackMessagePosted messagePosted, SlackSession session) {
        List<SlackAttachment> toBeAdded = Lists.newArrayList();

        String color = story.isSucceeded() ? slackColors.getSuccess() : slackColors.getFailed();
        /** Story Test Id**/
        if (story.testId().isPresent()) {
            SlackAttachment slackAttachment = new SlackAttachment("Test: ", "", story.testId().get(), null);
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
            String started = dateFormater.formatDate(story.started().get());
            String duration = (story.finished().isPresent())
                    ? dateFormater.formatHour(story.finished().get() - story.started().get())
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
            SlackAttachment slackAttachment = new SlackAttachment("--------- STEPS ---------", "", "", null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);

            /** Each Step **/
            story
                    .steps()
                    .stream()
                    .forEach(step -> {
                        SlackAttachment stepAttachment = new SlackAttachment(
                                step.title(),
                                "",
                                step.description().isPresent() ? step.description().get() : "",
                                null);
                        stepAttachment.setColor(slackColors.getWarning());
                        toBeAdded.add(stepAttachment);
                    });
        } else {
            SlackAttachment slackAttachment = new SlackAttachment("NO STEPS", "", "No @steps associated", null);
            slackAttachment
                    .setColor(color);
            toBeAdded.add(slackAttachment);
        }

        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, CHUNK_SIZE);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            /** Story Header **/
            String storyTitle = String.format(TITLE, serverName.toUpperCase());
            String desc = String.format("Part %s of %s", iteration + 1, partitions.size());
            SlackAttachment titleAttachment = new SlackAttachment(storyTitle, "", desc, null);
            titleAttachment
                    .setColor(slackColors.getInfo());

            SlackPreparedMessage.Builder partitionSend = new SlackPreparedMessage
                    .Builder()
                    .addAttachment(titleAttachment)
                    .addAttachments(partitions.get(index));
            session.sendMessage(
                    messagePosted.getChannel(),
                    partitionSend.build());
            iteration++;
        }
    }

    private void slackEmpty(SlackMessagePosted messagePosted, SlackSession session, List<String> arguments) {
        String title = String.format(TITLE, serverName.toUpperCase());
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
        SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
        slackAttachment
                .setColor(slackColors.getWarning());
        session
                .sendMessage(
                        messagePosted.getChannel(),
                        "",
                        slackAttachment);
    }

    @Override
    public String help() {
        return "story [server-name] [class name (optional)] [test name (optional)]: Gets the story for the test. " +
                "With no arguments will return last failed story";
    }
}
