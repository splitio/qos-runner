package io.split.qos.server.integrations.slack;

import com.slack.api.Slack;
import com.slack.api.bolt.App;
import com.slack.api.bolt.jetty.SlackAppServer;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.event.AppHomeOpenedEvent;

import java.util.List;

import static com.slack.api.model.block.Blocks.*;
import static com.slack.api.model.block.composition.BlockCompositions.markdownText;
import static com.slack.api.model.block.composition.BlockCompositions.plainText;
import static com.slack.api.model.view.Views.view;

public class SlackBolt {

    /**
     * Start slack server where users can interact with
     * @param serverName QOS server name
     */
    public static void startSlackServer(String serverName) {
        new Thread(() -> {
            App app = new App();

            app.command("/ping", (req, ctx) -> {
                return ctx.ack(":wave: pong "+ serverName);
            });

            app.event(AppHomeOpenedEvent.class, (payload, ctx) -> {
                var appHomeView = view(view -> view
                        .type("home")
                        .blocks(asBlocks(
                                header( header -> header.text(plainText(pt -> pt.emoji(true).text(
                                        "Bot for showing QOS test results"
                                ))))))
                );

                ctx.client().viewsPublish(r -> r
                        .userId(payload.getEvent().getUser())
                        .view(appHomeView)
                );

                return ctx.ack();
            });


            SlackAppServer server = new SlackAppServer(app);
            try {
                server.start(); // http://localhost:3000/slack/events
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not start slack server app", e);
            }

        }).start();
    }

    /**
     * Send a simple message to a channel
     * @param message The text to publish
     * @param channel Where you want the text to be published
     */
    public static void sendMessage(String message, String channel){
        try {
            var slack = Slack.getInstance().methods();
            slack.chatPostMessage(req -> req
                    .token(System.getenv("SLACK_BOT_TOKEN"))
                    .channel(channel)
                    .blocks(asBlocks(
                            section(section -> section.text(markdownText(message)))
                    ))
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not connect to slack", e);
        }
    }

    /**
     * Send a complex message to a channel
     * @param block A list of blocks which contains the message to publish
     * @param channel Where you want the text to be published
     */
    public static void sendMessage(List<LayoutBlock> block, String channel){
        try {
            var slack = Slack.getInstance().methods();
            slack.chatPostMessage(req -> req
                    .token(System.getenv("SLACK_BOT_TOKEN"))
                    .channel(channel)
                    .blocks(block)
            );
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not connect to slack", e);
        }
    }
}