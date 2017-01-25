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
        if (arguments.isEmpty()) {
            theStory = stories.getLatestStory();
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

        /** Story Title **/
        String desciption = String.format("Test started: %s, duration %s",
                dateFormater.formatDate(story.started()),
                dateFormater.formatHour(story.finished() - story.started()));
        SlackAttachment slackAttachment = new SlackAttachment(story.title(), "", desciption, null);
        slackAttachment
                .setColor(slackColors.getSuccess());
        toBeAdded.add(slackAttachment);

        /** Story Description **/
        slackAttachment = new SlackAttachment(story.description(), "", "", null);
        slackAttachment
                .setColor(slackColors.getSuccess());
        toBeAdded.add(slackAttachment);

        /** Each Step **/
        story
                .steps()
                .stream()
                .forEach(step -> {
                    SlackAttachment stepAttachment = new SlackAttachment(step.description(), "", "", null);
                    stepAttachment.setColor(slackColors.getWarning());
                    toBeAdded.add(stepAttachment);
                });

        List<List<SlackAttachment>> partitions = Lists.partition(toBeAdded, CHUNK_SIZE);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            /** Story Header **/
            String title = String.format(TITLE, serverName.toUpperCase());
            String description = String.format("Part %s of %s", iteration + 1, partitions.size());
            SlackAttachment titleAttachment = new SlackAttachment(title, "", desciption, null);
            slackAttachment
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
        } else if (arguments.size() == 1) {
            text = "Could not find story for test with name " + arguments.get(0);
        } else {
            text = String.format("Could not find story for test of class %s with name %s",
                    arguments.get(0),
                    arguments.get(1));
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
