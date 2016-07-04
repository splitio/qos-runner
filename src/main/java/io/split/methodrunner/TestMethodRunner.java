package io.split.methodrunner;

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
import io.split.methodrunner.commandline.MethodCommandLineArguments;
import io.split.methodrunner.modules.CommandLineArgumentsModule;
import io.split.methodrunner.modules.TestRunnerModule;
import io.split.methodrunner.modules.TestRunnerPropertiesModule;
import io.split.testrunner.junit.TestResult;
import io.split.testrunner.junit.JUnitRunnerFactory;
import io.split.testrunner.util.Util;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Singleton
public class TestMethodRunner implements Callable<List<TestResult>> {

    private static final Logger LOG = LoggerFactory.getLogger(TestMethodRunner.class);

    private final Method method;
    private final JUnitRunnerFactory testRunnerFactory;
    private final Integer quantity;
    private final List<TestResult> results;
    private final Integer parallel;
    private final ListeningExecutorService executor;
    private final int timeoutInMinutes;

    @Inject
    public TestMethodRunner(@Named(TestRunnerPropertiesModule.TIMEOUT_IN_MINUTES) String timeOutInMinutes,
                            MethodCommandLineArguments arguments,
                            JUnitRunnerFactory testRunnerFactory) {
        this.method = Preconditions.checkNotNull(arguments.test());
        this.testRunnerFactory = Preconditions.checkNotNull(testRunnerFactory);
        this.quantity = arguments.quantity();
        this.results = Collections.synchronizedList(Lists.newArrayList());
        this.parallel = arguments.parallel();
        this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallel));
        this.timeoutInMinutes = Integer.valueOf(Preconditions.checkNotNull(timeOutInMinutes));
    }

    @Override
    public List<TestResult> call() throws Exception {
        String testName = Util.id(method);
        long start = System.currentTimeMillis();
        LOG.info(String.format("STARTING TestMethodRunner %s, running it %s times %s in parallel", testName, quantity, parallel));
        for (int index = 0; index < this.quantity; index++) {
            Util.pause(Util.getRandom(500, 2000));
            ListenableFuture<TestResult> future = executor.submit(testRunnerFactory.create(method, Optional.of(String.valueOf(index))));
            Futures.addCallback(future, createCallback(method));
        }
        executor.awaitTermination(timeoutInMinutes, TimeUnit.MINUTES);
        LOG.info(String.format("FINISHED TestMethodRunner %s in %s", testName,Util.TO_PRETTY_FORMAT.apply(System.currentTimeMillis() - start)));
        return ImmutableList.copyOf(results);
    }

    private FutureCallback<TestResult> createCallback(Method method) {
        return new FutureCallback<TestResult>() {
            @Override
            public void onSuccess(TestResult result) {
                results.add(result);
                LOG.info(String.format("Test %s finished of %s", results.size(), quantity));
                if (results.size() == quantity) {
                    executor.shutdown();
                }
                processOutput(result.getOut());
            }

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
        TestMethodRunner runner = injector.getInstance(TestMethodRunner.class);
        List<TestResult> results = runner.call();
        List<TestResult> failed = failedTests(results);
        if (!failed.isEmpty()) {
            System.exit(1);
        }
    }

    @VisibleForTesting
    public static Injector createInjector(String[] args) {
        CommandLineArgumentsModule commandLineArgumentsModule = new CommandLineArgumentsModule(args);
        List<Module> modules = Lists.newArrayList(commandLineArgumentsModule,
                new TestRunnerPropertiesModule(commandLineArgumentsModule.propertiesPath()),
                new TestRunnerModule());

        return Guice.createInjector(modules);
    }

    private static List<TestResult> failedTests(List<TestResult> results) {
        return results.stream()
                .filter(result -> !result.getResult().wasSuccessful())
                .collect(Collectors.toList());
    }
}
