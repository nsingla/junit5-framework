package io.nsingla.junit5.extensions;

import org.opentest4j.TestAbortedException;

public class TestRetriedException extends TestAbortedException {

    public TestRetriedException(int count, Throwable throwable) {
        super("Retrying test (x" + count + ")", throwable);
    }
}
