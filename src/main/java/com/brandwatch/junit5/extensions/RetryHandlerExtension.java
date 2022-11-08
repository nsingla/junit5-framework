package com.brandwatch.junit5.extensions;

import static org.apache.commons.collections4.SetUtils.unmodifiableSet;

import com.brandwatch.junit5.annotations.RetryFailedParameterizedTest;
import com.brandwatch.junit5.extensions.internal.contexts.GenericTestTemplateInvocationContext;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.AnnotationUtils;
import org.opentest4j.TestAbortedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Set;

/**
 * {@link RetryHandlerExtension} responsible of handling failures
 * and retrying failed test cases for up to {@link #MAX_RETRY} times.
 *
 * <ul>
 *     <li>Original exception is re-thrown after {@link #MAX_RETRY} number of failures.</li>
 *     <li>Retried tests are always marked as skipped, while {@link TestRetriedException} holds the original exception.</li>
 *     <li>Retrying is skipped in case exception is one of {@link #IGNORED_EXCEPTIONS}.</li>
 * </ul>
 *
 * @param <T> {@link RetryHandlerExtension} type
 */
public class RetryHandlerExtension<T extends GenericTestTemplateInvocationContext<T>> implements TestExecutionExceptionHandler, AfterEachCallback {

    protected static final Logger logger = LoggerFactory.getLogger(RetryHandlerExtension.class);

    public static final int MAX_RETRY = Integer.parseInt(System.getProperty("retryCount", "0"));

    public static final Set<Class<?>> IGNORED_EXCEPTIONS = unmodifiableSet(JUnitException.class, TestAbortedException.class, SkipRetryException.class);

    private final T invocationContext;

    public RetryHandlerExtension(T context) {
        this.invocationContext = context;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        // Throw the original exception if test execution is aborted or maximum number of retry reached and do not retry
        if (!invocationContext.isMaxFailureCountReached(MAX_RETRY) && !isIgnoredException(throwable)) {
            throw new TestRetriedException(invocationContext.getFailuresCount() + 1, throwable);
        }
        throw throwable;
    }

    @Override
    public void afterEach(ExtensionContext context) {
        // Retry only if original exception is wrapped inside TestRetriedException
        if (context.getExecutionException().isPresent() && context.getExecutionException().get() instanceof TestRetriedException) {
            invocationContext.queueForRetry();
        } else {
            invocationContext.queueForClose();
        }
    }

    /**
     * Checks if the provided exception is on ignore list of {@link #IGNORED_EXCEPTIONS}.
     *
     * @param throwable {@link Throwable} exception to check
     * @return true if exception should be ignored
     */
    private boolean isIgnoredException(Throwable throwable) {
        Class<?> clazz = throwable.getClass();
        return IGNORED_EXCEPTIONS.stream()
            .anyMatch(ignoredException -> ignoredException.isAssignableFrom(clazz));
    }
}
