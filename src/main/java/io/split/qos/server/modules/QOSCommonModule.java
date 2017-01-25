package io.split.qos.server.modules;

import com.google.common.base.Preconditions;
import com.google.inject.AbstractModule;
import io.split.qos.server.QOSServerState;
import io.split.qos.server.integrations.slack.SlackCommon;
import io.split.qos.server.stories.QOSStories;
import io.split.qos.server.stories.Story;
import io.split.testrunner.util.TestsFinder;

/**
 * Module used to inject from the Server Guice Injector into the Test Injectors
 *
 * <p>
 *     Using Guice.createChildInjector is causing troubles since some properties are imported both in the server and in
 *     the tests.
 * </p>
 */
public class QOSCommonModule extends AbstractModule {

    private final SlackCommon slackCommon;
    private final QOSServerState state;
    private final QOSStories stories;
    private final TestsFinder testFinder;
    private final Story story;

    public QOSCommonModule(SlackCommon slackCommon,
                           QOSServerState serverState,
                           QOSStories stories,
                           TestsFinder testsFinder,
                           Story story) {
        this.slackCommon = Preconditions.checkNotNull(slackCommon);
        this.state = Preconditions.checkNotNull(serverState);
        this.stories = Preconditions.checkNotNull(stories);
        this.testFinder = Preconditions.checkNotNull(testsFinder);
        this.story = Preconditions.checkNotNull(story);
    }

    @Override
    protected void configure() {
        bind(SlackCommon.class).toInstance(slackCommon);
        bind(QOSServerState.class).toInstance(state);
        bind(QOSStories.class).toInstance(stories);
        bind(TestsFinder.class).toInstance(testFinder);
        bind(Story.class).toInstance(story);
    }
}
