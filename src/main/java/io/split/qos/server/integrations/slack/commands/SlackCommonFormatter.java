package io.split.qos.server.integrations.slack.commands;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.qos.server.modules.QOSPropertiesModule;

import java.util.List;
import java.util.Optional;

@Singleton
public class SlackCommonFormatter {

    private final int messageGrouping;

    @Inject
    public SlackCommonFormatter(
            @Named(QOSPropertiesModule.MESSAGE_GROUPING) String messageGrouping) {
        this.messageGrouping = Integer.valueOf(messageGrouping);
    }

    public List<String> groupMessage(List<String> lines) {
        return groupMessage(Optional.empty(), lines);
    }

    public List<String> groupMessage(String header, List<String> lines) {
        return groupMessage(Optional.of(header), lines);
    }

    private List<String> groupMessage(Optional<String> header, List<String> lines) {
        Preconditions.checkNotNull(lines);

        List<String> result = Lists.newArrayList();
        int end = lines.size();
        for(int slice = 0; slice <= (end - 1) / messageGrouping; slice++) {
            StringBuilder group = new StringBuilder();
            group.append("```\n");
            if (header.isPresent()) {
                group.append(header.get() + "\n");
            }
            int upTo = Math.min(messageGrouping, end - slice * messageGrouping);
            for(int begin = 0; begin < upTo; begin++) {
                group.append(String.format("%s\n", lines.get(slice * messageGrouping + begin)));
            }
            group.append("```\n");
            result.add(group.toString());
        }
        return result;
    }
}
