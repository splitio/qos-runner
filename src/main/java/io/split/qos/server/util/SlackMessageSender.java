package io.split.qos.server.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import io.split.qos.server.modules.QOSServerModule;
import io.split.testrunner.util.SlackColors;

import java.util.List;

@Singleton
public class SlackMessageSender {
    private static final int CHUNK_SIZE = 50;
    private final String serverName;
    private final SlackColors colors;

    @Inject
    public SlackMessageSender(
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = Preconditions.checkNotNull(slackColors);
    }


    public void send(String command,
                     SlackSession session,
                     SlackChannel slackChannel,
                     List<SlackAttachment> attachments) {
        send(command, "", session, slackChannel, attachments);
    }

    public void send(String command,
                     String description,
                     SlackSession session,
                     SlackChannel slackChannel) {
        send(command, description, session, slackChannel, Lists.newArrayList());
    }

    public void send(String command,
                     String description,
                     SlackSession session,
                     SlackChannel slackChannel,
                     List<SlackAttachment> attachments) {
        Preconditions.checkNotNull(attachments);
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(description);
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(slackChannel);
        String title = String.format("[%s] %s", serverName.toUpperCase(), command.toUpperCase());
        SlackAttachment slackAttachment = new SlackAttachment(title, "", description, null);
        slackAttachment.setColor(colors.getInfo());

        SlackPreparedMessage.Builder partitionSend = new SlackPreparedMessage
                .Builder()
                .addAttachment(slackAttachment);

        if (!attachments.isEmpty()) {
            partitionSend.addAttachments(attachments);
        }

        session.sendMessage(
                slackChannel,
                partitionSend.build());
    }

    public void sendPartition(String command, SlackSession session,
                     SlackChannel slackChannel, List<SlackAttachment> attachments) {
        Preconditions.checkNotNull(attachments);
        Preconditions.checkNotNull(command);
        Preconditions.checkNotNull(session);
        Preconditions.checkNotNull(slackChannel);
        List<List<SlackAttachment>> partitions = Lists.partition(attachments, CHUNK_SIZE);
        int iteration = 0;
        for(int index = 0; index < partitions.size(); index++) {
            String title = String.format("[%s] %s", serverName.toUpperCase(), command.toUpperCase());
            String text = String.format("Total %s, items %s - %s",
                    attachments.size(),
                    1 + CHUNK_SIZE * iteration,
                    CHUNK_SIZE * iteration + partitions.get(index).size());

            SlackAttachment slackAttachment = new SlackAttachment(title, "", text, null);
            slackAttachment.setColor(colors.getInfo());

            SlackPreparedMessage.Builder partitionSend = new SlackPreparedMessage
                    .Builder()
                    .addAttachment(slackAttachment)
                    .addAttachments(partitions.get(index));
            session.sendMessage(
                    slackChannel,
                    partitionSend.build());
            iteration++;
        }
    }

    public void sendInfo(String title, String text, SlackChannel channel, SlackSession session) {
        send(title, text, colors.getInfo(), channel, session);
    }

    public void sendSuccess(String title, String text, SlackChannel channel, SlackSession session) {
        send(title, text, colors.getSuccess(), channel, session);
    }

    public void sendWarning(String title, String text, SlackChannel channel, SlackSession session) {
        send(title, text, colors.getWarning(), channel, session);
    }

    public void sendFailed(String title, String text, SlackChannel channel, SlackSession session) {
        send(title, text, colors.getFailed(), channel, session);
    }

    private void send(String title, String text, String color, SlackChannel channel, SlackSession session) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(channel);
        Preconditions.checkNotNull(session);
        String titleWithServerName = String.format("[%s] %s" , serverName.toUpperCase(), title.toUpperCase());
        SlackAttachment slackAttachment = new SlackAttachment(titleWithServerName, "", text, null);
        slackAttachment
                .setColor(color);
        session
                .sendMessage(
                        channel,
                        "",
                        slackAttachment);
    }
}
