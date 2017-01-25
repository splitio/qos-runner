package io.split.qos.server.stories;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.stories.annotations.Title;
import io.split.testrunner.util.TestsFinder;
import io.split.testrunner.util.Util;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Declare this rule if you want to have Stories.
 */
public class StoriesRule implements TestRule {

    private final QOSStories stories;
    private final Story story;
    private final TestsFinder testFinder;
    private final String suitesPackage;
    private final List<String> suites;

    @Inject
    public StoriesRule(QOSStories stories,
                       @Named(QOSPropertiesModule.SUITES) String suites,
                       @Named(QOSPropertiesModule.SUITES_PACKAGE) String suitesPackage,
                       TestsFinder testsFinder,
                       Story story) {
        this.stories = Preconditions.checkNotNull(stories);
        this.testFinder = Preconditions.checkNotNull(testsFinder);
        this.suites = Arrays.asList(Preconditions.checkNotNull(suites).split(","));
        this.suitesPackage = Preconditions.checkNotNull(suitesPackage);
        this.story = story;
    }

    @Override
    public Statement apply(Statement statement, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    setStoryTitleAndDescription(description);

                    try {
                        statement.evaluate();
                        story.finish(System.currentTimeMillis());
                        story.setSucceeded(true);
                    } catch (Throwable e) {
                        story.setSucceeded(false);
                        throw e;
                    }
                } finally {
                    stories.addStory(description, story);
                }
            }
        };
    }

    @VisibleForTesting
    void setStoryTitleAndDescription(Description description) throws Exception {
        Optional<Method> optional = findTestFromDescription(description);
        story.start(System.currentTimeMillis());
        if (optional.isPresent()) {
            Method method = optional.get();
            getTitle(method)
                    .ifPresent(title -> story.title(title));
            getDescription(method)
                    .ifPresent(desc -> story.description(desc));
            story.testId(Util.id(description));
        }
    }

    private Optional<String> getTitle(Method method) {
        if (method.isAnnotationPresent(Title.class)) {
            return Optional.of(method.getAnnotation(Title.class).value());
        } else {
            return Optional.empty();
        }
    }

    private Optional<String> getDescription(Method method) {
        if (method.isAnnotationPresent(io.split.qos.server.stories.annotations.Description.class)) {
            return Optional.of(method.getAnnotation(io.split.qos.server.stories.annotations.Description.class).value());
        } else {
            return Optional.empty();
        }
    }

    private Optional<Method> findTestFromDescription(Description description) throws Exception {
        return testFinder
                .getTestMethodsOfPackage(suites, suitesPackage)
                .stream()
                .filter(method -> {
                    return method.getName().equals(description.getMethodName())
                            && method.getDeclaringClass().getName().equals(description.getClassName());
                })
                .findAny();

    }
}
