package io.split.qos.server;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.ullink.slack.simpleslackapi.SlackAttachment;
import io.split.qos.server.integrations.IntegrationServerFactory;
import io.split.qos.server.integrations.slack.broadcaster.SlackBroadcaster;
import io.split.qos.server.integrations.slack.commandintegration.SlackCommandIntegration;
import io.split.qos.server.modules.QOSPropertiesModule;
import io.split.qos.server.modules.QOSServerModule;
import io.split.qos.server.pausable.PausableScheduledThreadPoolExecutor;
import io.split.testrunner.TestResult;
import io.split.testrunner.TestRunner;
import io.split.testrunner.TestRunnerFactory;
import io.split.testrunner.util.TestsFinder;
import io.split.testrunner.util.Util;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.TimeUnit;

/**
 * Main Class that actually run the tests.
 */
@Singleton
public class QOSServerBehaviour implements Callable<Void>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(QOSServerBehaviour.class);
    private static final String SEPARATOR = "-------------------------------------------------------------";

    private final int parallelTests;
    private final PausableScheduledThreadPoolExecutor pausableExecutor;
    private final ListeningScheduledExecutorService executor;
    private final QOSServerState state;
    private final int shutdownWaitInMinutes;
    private final int delayBetweenInSeconds;
    private final boolean spreadTests;
    private final List<String> suites;
    private final String suitesPackage;
    private final TestRunnerFactory testRunnerFactory;
    private final SlackCommandIntegration commandIntegration;
    private final SlackBroadcaster broadcastIntegration;
    private final String serverName;
    private final QOSTestsTracker tracker;
    private final Integer delayBetweenInSecondsWhenFail;

    @Inject
    public QOSServerBehaviour(
            @Named(QOSPropertiesModule.DELAY_BETWEEN_IN_SECONDS) String delayBetweenInSeconds,
            @Named(QOSPropertiesModule.DELAY_BETWEEN_IN_SECONDS_WHEN_FAIL) String delayBetweenInSecondsWhenFail,
            @Named(QOSPropertiesModule.SPREAD_TESTS) String spreadTests,
            @Named(QOSPropertiesModule.PARALLEL_TESTS) String parallelTests,
            @Named(QOSPropertiesModule.SHUTDOWN_WAIT_IN_MINUTES) String shutdownWaitInMinutes,
            @Named(QOSPropertiesModule.SUITES) String suites,
            @Named(QOSPropertiesModule.SUITES_PACKAGE) String suitesPackage,
            @Named(QOSServerModule.QOS_SERVER_NAME) String serverName,
            TestRunnerFactory testRunnerFactory,
            IntegrationServerFactory integrationServerFactory,
            QOSServerState state,
            QOSTestsTracker tracker) {

        this.delayBetweenInSeconds = Integer.valueOf(Preconditions.checkNotNull(delayBetweenInSeconds));
        this.delayBetweenInSecondsWhenFail = Integer.valueOf(Preconditions.checkNotNull(delayBetweenInSecondsWhenFail));
        this.spreadTests = Boolean.valueOf(Preconditions.checkNotNull(spreadTests));
        this.shutdownWaitInMinutes = Integer.valueOf(Preconditions.checkNotNull(shutdownWaitInMinutes));
        this.parallelTests = Integer.valueOf(Preconditions.checkNotNull(parallelTests));
        this.pausableExecutor = new PausableScheduledThreadPoolExecutor(this.parallelTests);
        this.executor = MoreExecutors.listeningDecorator(pausableExecutor);
        this.state = Preconditions.checkNotNull(state);
        this.suites = Arrays.asList(Preconditions.checkNotNull(suites).split(","));
        this.suitesPackage = Preconditions.checkNotNull(suitesPackage);
        this.testRunnerFactory = Preconditions.checkNotNull(testRunnerFactory);
        this.commandIntegration = Preconditions.checkNotNull(integrationServerFactory).slackCommandIntegration();
        this.broadcastIntegration = Preconditions.checkNotNull(integrationServerFactory).slackBroadcastIntegration();
        this.serverName = Preconditions.checkNotNull(serverName);
        this.pause("Initialization");
        this.tracker = Preconditions.checkNotNull(tracker);
    }

    /**
     * Steps are:
     * <ul>
     *     <li> Do all the initialization</li>
     *     <li> Find all the classes that are annotated with the suite, then find all the tests of those classes</li>
     *     <li> Add each test to an executor. The tests will be spread if spreadTests is enabled</li>
     *     <li> When each tests finished, it will be readded to the executor, with some delay specificed in delayBetweenInSeconds</li>
     * </ul>
     */
    @Override
    public Void call() throws Exception {
        LOG.info(String.format("STARTING QOS Server for suites %s, running %s tests in parallel", suites, parallelTests));
        if (commandIntegration.isEnabled()) {
            commandIntegration.initialize();
            commandIntegration.startBotListener();
        }
        if (broadcastIntegration.isEnabled()) {
            broadcastIntegration.initialize();
        }

        List<Method> methodsToTest = TestsFinder.getTestMethodsOfPackage(suites, suitesPackage);
        int total = methodsToTest.size();
        int schedule = 0;
        if (total == 0) {
            LOG.error("Could not find tests to run on " + suites + " package " + suitesPackage);
            if (broadcastIntegration.isEnabled()) {
                String message = String.format("No tests found for %s, suites %s, package %s", serverName, suites, suitesPackage);
                SlackAttachment slackAttachment = new SlackAttachment("NO TESTS WILL RUN FOR " + serverName, "", message, null);
                slackAttachment
                    .setColor("warning");

                broadcastIntegration.broadcastVerbose("", slackAttachment);
            }
            return null;
        }
        int step = (spreadTests) ? delayBetweenInSeconds / total : 1;
        LOG.info(String.format("Test Methods to run: %s, total tests to run %s, delay %s seconds, step between each test %s seconds",
                methodsToTest, total, delayBetweenInSeconds, step));

        for (Method method : methodsToTest) {
            state.registerTest(method);
            TestRunner testRunner = testRunnerFactory.create(method);
            ListenableFuture<TestResult> future = executor.schedule(
                    testRunner,
                    schedule,
                    TimeUnit.SECONDS);

            tracker.track(method, testRunner, future);
            Futures.addCallback(future, createCallback(
                    method,
                    delayBetweenInSeconds,
                    delayBetweenInSecondsWhenFail,
                    delayBetweenInSeconds));
            schedule += step;
        }
        if (broadcastIntegration.isEnabled()) {
            String message = String.format("QOS Server '%s' is up", serverName);
            SlackAttachment slackAttachment = new SlackAttachment(
                    String.format("[%s] UP", serverName.toUpperCase()), "", message, null);
            slackAttachment
                    .setColor("good");

            broadcastIntegration.broadcastVerbose("", slackAttachment);
        }
        resume("Initialization");
        return null;
    }

    public void pause(String who) {
        pausableExecutor.pause();
        state.pause(who);
    }

    public void resume(String who) {
        pausableExecutor.resume();
        state.resume(who);
    }

    private FutureCallback<TestResult> createCallback(Method method, int when, int ifFailed, int afterFirst) {
        return new FutureCallback<TestResult>() {
            /**
             * This is where tests are readded to the executor.
             */
            @Override
            public void onSuccess(TestResult result) {
                int triggerAgain = result.getResult().wasSuccessful()? when : ifFailed;
                LOG.info(String.format("%s finished, rerunning in %s seconds",
                        method.getName(), triggerAgain));
                TestRunner runner = testRunnerFactory.create(method);
                ListenableFuture<TestResult> future = executor.schedule(
                        runner,
                        triggerAgain,
                        TimeUnit.SECONDS);
                tracker.track(method, runner, future);
                Futures.addCallback(future, createCallback(method, afterFirst, ifFailed, afterFirst));
                processOutput(result.getOut());
            }

            /**
             * For debugging, if it was cancelled do not print anything.
             *
             * @param t the throwable that caused the error.
             */
            @Override
            public void onFailure(Throwable t) {
                if (t instanceof CancellationException) {
                    return;
                }
                System.out.println(SEPARATOR);
                System.out.println(SEPARATOR);
                System.out.println(SEPARATOR);
                System.out.println("UNEXPECTED FAILURE");
                System.out.println(String.format("FAILED %s#%s", method.getDeclaringClass(), method.getName()));
                System.out.println("REASON: " + t.getMessage());
                System.out.println("STACKTRACE:");
                System.out.println(ExceptionUtils.getStackTrace(t));
                System.out.println(SEPARATOR);
                System.out.println(SEPARATOR);
                System.out.println(SEPARATOR);
            }

            private void processOutput(ByteArrayOutputStream outputStream) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                int oneByte;
                synchronized (System.out) {
                    while ((oneByte = inputStream.read()) != -1) {
                        System.out.write(oneByte);
                    }
                }
            }
        };
    }

    @Override
    public void close() throws Exception {
        // Doesnt disconnect
        /**
        if (commandIntegration.isEnabled()) {
            commandIntegration.close();
        }
        if (!commandIntegration.isEnabled() && broadcastIntegration.isEnabled()) {
            broadcastIntegration.close();
        }
         */
        if (pausableExecutor != null) {
            pausableExecutor.resume();
            pausableExecutor.shutdownNow();
            pausableExecutor.awaitTermination(shutdownWaitInMinutes, TimeUnit.MINUTES);
        }
    }

    public List<Method> runAllNow() {
        List<Method> rerunning = Lists.newArrayList();
        List<QOSTestsTracker.Tracked> notRunning = tracker.getNotRunning();
        if (notRunning.isEmpty()) {
            return rerunning;
        }
        int step = (spreadTests) ? delayBetweenInSeconds / notRunning.size() : 1;
        int schedule = step;
        for(QOSTestsTracker.Tracked track : notRunning) {
            track.future().cancel(true);
            rerunning.add(track.method());
            LOG.info(String.format("%s canceled, rerunning now",
                    Util.id(track.method())));
            TestRunner runner = testRunnerFactory.create(track.method());
            ListenableFuture<TestResult> future = executor.schedule(
                    runner,
                    0,
                    TimeUnit.SECONDS);
            tracker.track(track.method(), runner, future);
            Futures.addCallback(future, createCallback(track.method(), schedule, delayBetweenInSecondsWhenFail, delayBetweenInSeconds));
            schedule += step;
        }
        return rerunning;
    }

    public Optional<Method> runTestNow(Optional<String> fuzzyClass, String fuzzyName) {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(fuzzyName));
        Preconditions.checkNotNull(fuzzyClass);
        Optional<QOSTestsTracker.Tracked> present = tracker
                .getAll()
                .stream()
                .filter(tracked -> {
                    if (fuzzyClass.isPresent() &&
                            (!tracked.method().getDeclaringClass().getName().contains(fuzzyClass.get()))) {
                        return false;
                    }
                    return (fuzzyName.contains(tracked.method().getName()));
                })
                .findAny();
        if (!present.isPresent()) {
            return Optional.empty();
        } else {
            QOSTestsTracker.Tracked tracked = present.get();
            if (tracked.runner().isRunning()) {
                // It is already running
                return Optional.of(tracked.method());
            }
            LOG.info(String.format("Running now %s",
                    Util.id(tracked.method())));
            TestRunner runner = testRunnerFactory.create(tracked.method());
            ListenableFuture<TestResult> future = executor.schedule(
                    runner,
                    0,
                    TimeUnit.SECONDS);
            tracker.track(tracked.method(), runner, future);
            Futures.addCallback(future, createCallback(tracked.method(), delayBetweenInSeconds, delayBetweenInSecondsWhenFail, delayBetweenInSeconds));
            return Optional.of(tracked.method());
        }
    }
}
