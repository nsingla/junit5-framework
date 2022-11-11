package io.nsingla.junit5.extensions;

public abstract class SkipRetryException extends RuntimeException {

    protected SkipRetryException(String message) {
        super(message);
    }

    protected SkipRetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
