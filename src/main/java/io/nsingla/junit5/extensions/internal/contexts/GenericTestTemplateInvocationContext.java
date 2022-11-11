package io.nsingla.junit5.extensions.internal.contexts;

import io.nsingla.junit5.extensions.RetryHandlerExtension;
import io.nsingla.junit5.extensions.internal.iterators.TestTemplateIterator;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.Collections;
import java.util.List;

/**
 * Base {@link TestTemplateInvocationContext} that's holding basic information
 * and providing additional extensions for handling failures and retrying tests.
 *
 * @param <T> {@link GenericTestTemplateInvocationContext} type
 */
public abstract class GenericTestTemplateInvocationContext<T extends GenericTestTemplateInvocationContext<T>> implements TestTemplateInvocationContext {

    protected final TestTemplateIterator<T> iterator;
    protected final int failuresCount;

    GenericTestTemplateInvocationContext(TestTemplateIterator<T> iterator, int failuresCount) {
        this.iterator = iterator;
        this.failuresCount = failuresCount;
    }

    @Override
    public String getDisplayName(int invocationIndex) {
        String displayName = getDisplayName();
        return failuresCount == 0
            ? displayName
            : displayName + "[retry " + failuresCount + "]";
    }

    /**
     * Returns display name for this invocation context.
     *
     * @return {@link String} display name
     */
    abstract String getDisplayName();

    /**
     * Returns a new instance of this invocation context for test retry.
     *
     * @return new instance of {@code T}
     */
    abstract T getNextInvocationContext();

    /**
     * Returns a new extension that's responsible of handling
     * failures and invoking a new test run if previous failed.
     *
     * @return new instance of {@link RetryHandlerExtension} extension object
     */
    abstract RetryHandlerExtension<T> getRetryHandlerExtension();

    @Override
    public List<Extension> getAdditionalExtensions() {
        return Collections.singletonList(getRetryHandlerExtension());
    }

    /**
     * Returns failures count in the current invocation context.
     *
     * @return number of failures
     */
    public int getFailuresCount() {
        return failuresCount;
    }

    /**
     * Checks if maximum number of failures is reached for the current context.
     *
     * @param maxFailuresCount maximum number of failures
     * @return true if max failures limit is reached
     */
    public boolean isMaxFailureCountReached(int maxFailuresCount) {
        return failuresCount >= maxFailuresCount;
    }

    /**
     * Queue the current invocation context for retry.
     */
    public void queueForRetry() {
        iterator.add(getNextInvocationContext());
    }

    /**
     * Queue the current invocation context for close.
     * It shouldn't queue for retry anymore once it's marked for closing.
     */
    public void queueForClose() {
        iterator.tryClose();
    }
}
