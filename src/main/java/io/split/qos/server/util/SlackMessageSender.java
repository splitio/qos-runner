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
public class SlackAttachmentPartitioner {
    private static final int CHUNK_SIZE = 50;
    private final String serverName;
    private final SlackColors colors;

    @Inject
    public SlackAttachmentPartitioner(
            SlackColors slackColors,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName) {
        this.serverName = Preconditions.checkNotNull(serverName);
        this.colors = Preconditions.checkNotNull(slackColors);
    }

    public void send(String command, SlackSession session,
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
}
