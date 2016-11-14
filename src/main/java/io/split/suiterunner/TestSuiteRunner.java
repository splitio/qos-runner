package io.split.suiterunner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.split.suiterunner.commandline.SuiteCommandLineArguments;
import io.split.suiterunner.modules.SuiteCommandLineArgumentsModule;
import io.split.suiterunner.modules.SuiteRunnerPropertiesModule;
import io.split.testrunner.junit.JUnitRunnerFactory;
import io.split.testrunner.junit.TestResult;
import io.split.testrunner.junit.modules.TestRunnerModule;
import io.split.testrunner.util.GuiceInitializator;
import io.split.testrunner.util.TestsFinder;
import io.split.testrunner.util.Util;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class TestSuiteRunner implements Callable<List<TestResult>> {
    private static final Logger LOG = LoggerFactory.getLogger(TestSuiteRunner.class);

    private final JUnitRunnerFactory testRunnerFactory;
    private final List<String> suites;
    private final String suitesPackage;
    private final int timeoutInMinutes;
    private final ListeningExecutorService executor;
    private final List<TestResult> results;
    private final int parallel;
    private int totalTests;

    @Inject
    public TestSuiteRunner(
            @Named(SuiteRunnerPropertiesModule.TIMEOUT_IN_MINUTES) String timeOutInMinutes,
            SuiteCommandLineArguments arguments,
            JUnitRunnerFactory testRunnerFactory) {
        this.testRunnerFactory = Preconditions.checkNotNull(testRunnerFactory);
        this.suites = arguments.suites();
        this.suitesPackage = arguments.suitesPackage();
        this.timeoutInMinutes = Integer.valueOf(timeOutInMinutes);
        this.parallel = arguments.parallel();
        this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallel));
        this.totalTests = 0;
        this.results = Collections.synchronizedList(Lists.newArrayList());
    }

    @Override
    public List<TestResult> call() throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        LOG.info(String.format("STARTING TestSuiteRunner for suites [%s], running %s tests in parallel", suites, parallel));
        List<Class> classesToTest = TestsFinder.getTestClassesOfPackage(suites, suitesPackage);
        LOG.info(String.format("Test Classes to run: %s", classesToTest));
        classesToTest.stream()
                .forEach(test -> Lists.newArrayList(test.getMethods())
                        .stream()
                        .filter(method -> method.isAnnotationPresent(Test.class)
                                && !method.isAnnotationPresent(Ignore.class))

                        .forEach(method -> {
                            totalTests++;
                            Util.pause(Util.getRandom(500, 2000));
                            ListenableFuture<TestResult> future = executor.submit(testRunnerFactory.create(method, Optional.empty()));
                            Futures.addCallback(future, createCallback(method));
                        }));
        LOG.info(String.format("Total tests running: %s", totalTests));
        executor.awaitTermination(timeoutInMinutes, TimeUnit.MINUTES);
        LOG.info(String.format("FINISHED TestSuiteRunner for suites [%s] in %s", suites, Util.TO_PRETTY_FORMAT.apply(System.currentTimeMillis() - start)));
        return ImmutableList.copyOf(results);
    }

    /**
     * Creates a callback that is used to retry tests that fail.
     *
     * @param method the test to run.
     * @return the Callback that will retry if a test fails.
     */
    private FutureCallback<TestResult> createCallback(Method method) {
        return new FutureCallback<TestResult>() {
            @Override
            public void onSuccess(TestResult result) {
                results.add(result);
                LOG.info(String.format("Test %s finished of %s", results.size(), totalTests));
                if (results.size() == totalTests) {
                    executor.shutdown();
                }
                processOutput(result.getOut());
            }

            /**
             * For debugging, if everything goes well should never happen.
             *
             * @param t the throwable that caused the error.
             */
            @Override
            public void onFailure(Throwable t) {
                System.out.println("-------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------");
                System.out.println("UNEXPECTED FAILURE");
                System.out.println(String.format("FAILED %s#%s", method.getDeclaringClass(), method.getName()));
                System.out.println("REASON: " + t.getMessage());
                System.out.println("STACKTRACE:");
                System.out.println(ExceptionUtils.getStackTrace(t));
                System.out.println("-------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------");
                System.out.println("-------------------------------------------------------------");
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


    public static void main(String[] args) throws Exception {
        Preconditions.checkNotNull(args);
        Injector injector = createInjector(args);

        TestSuiteRunner runner = injector.getInstance(TestSuiteRunner.class);
        List<TestResult> results = runner.call();
        List<TestResult> failed = failedTests(results);
        if (!failed.isEmpty()) {
            LOG.info("FAILURES:");
            failed
                    .stream()
                    .forEach(testResult -> testResult
                            .getResult()
                            .getFailures()
                            .stream()
                            .forEach(failure -> {
                                LOG.info("---------------------------------------------------------");
                                LOG.info(Util.id(failure.getDescription()));
                                LOG.info(failure.getTestHeader());
                                LOG.info(failure.getMessage());
                                LOG.info(failure.getTrace());
                            }));
            System.exit(1);
        }
    }

    /**
     * Given the command line arguments, it creates the guice injector.
     *
     * @param args Command line arguments.
     *
     * @return the injector used.
     */
    @VisibleForTesting
    public static Injector createInjector(String[] args) {
        SuiteCommandLineArgumentsModule suiteCommandLineArgumentsModule= new SuiteCommandLineArgumentsModule(args);
        GuiceInitializator.setPath(suiteCommandLineArgumentsModule.propertiesPath());
        GuiceInitializator.setSuite();
        List<Module> modules = Lists.newArrayList(suiteCommandLineArgumentsModule,
                new SuiteRunnerPropertiesModule(),
                new TestRunnerModule());

        return Guice.createInjector(modules);
    }


    private static List<TestResult> failedTests(List<TestResult> results) {
        return results.stream()
                .filter(result -> !result.getResult().wasSuccessful())
                .collect(Collectors.toList());
    }
}
