package io.split.qos.server.testrunner;

import org.junit.runner.Result;

import java.io.ByteArrayOutputStream;

/**
 * Composition that also contains the output stream, besides the result.
 */
public class QOSTestResult {
    private final Result result;
    private final ByteArrayOutputStream out;

    public QOSTestResult(Result result, ByteArrayOutputStream out) {
        this.result = result;
        this.out = out;
    }

    public Result getResult() {
        return result;
    }

    public ByteArrayOutputStream getOut() {
        return out;
    }
}
