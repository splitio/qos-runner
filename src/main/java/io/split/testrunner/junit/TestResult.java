package io.split.testrunner.junit;

import org.junit.runner.Result;

import java.io.ByteArrayOutputStream;

/**
 * Composition that also contains the output stream, besides the result.
 */
public class TestResult {
    private final Result result;
    private final ByteArrayOutputStream out;

    public TestResult(Result result, ByteArrayOutputStream out) {
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
