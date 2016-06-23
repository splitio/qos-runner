package io.split.qos.server.testrunner;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.split.qos.server.util.Util;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Simply encapsulates JUniteCore.run
 */
public class QOSTestRunner implements Callable<QOSTestResult> {
    private static final Log LOG = LogFactory.getLog(QOSTestRunner.class);

    private final Method test;
    private final ByteArrayOutputStream outputStream;
    private boolean running;

    @Inject
    public QOSTestRunner(
            @Assisted Method test,
            ByteArrayOutputStream outputStream) {
        this.test = Preconditions.checkNotNull(test);
        this.outputStream = Preconditions.checkNotNull(outputStream);
        this.running = false;
    }

    /**
     * Wrapper over JUniteCore that runs one test.
     *
     * @return the result of the test.
     * @throws Exception if there was an exception running the test.
     */
    @Override
    public QOSTestResult call() throws Exception {
        this.running = true;
        JUnitCore jUnitCore = getJUnitCore();
        String testName = String.format("%s#%s", test.getDeclaringClass().getCanonicalName(), test.getName());
        long start = System.currentTimeMillis();
        try {
            LOG.info(String.format("STARTING Test %s", testName));
            Result result = jUnitCore.run(Request.method(test.getDeclaringClass(), test.getName()));
            LOG.info(String.format("FINSHED Test %s in %s, result %s", testName,
                    Util.TO_PRETTY_FORMAT.apply(System.currentTimeMillis() - start), result.wasSuccessful()? "SUCCEEDED" : "FAILED"));
            return new QOSTestResult(result, outputStream);
        } finally {
            outputStream.close();
        }
    }

    public boolean isRunning() {
        return running;
    }

    private JUnitCore getJUnitCore() throws FileNotFoundException {
        return new JUnitCore();
    }
}
