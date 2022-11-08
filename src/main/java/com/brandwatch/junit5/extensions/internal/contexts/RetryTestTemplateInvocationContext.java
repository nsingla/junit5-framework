package com.brandwatch.junit5.extensions.internal.contexts;

import com.brandwatch.junit5.annotations.RetryFailedTest;
import com.brandwatch.junit5.extensions.RetryHandlerExtension;
import com.brandwatch.junit5.extensions.internal.iterators.TestTemplateIterator;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

/**
 * {@link TestTemplateInvocationContext} that's holding necessary information
 * and providing additional extensions for handling failures and retrying non-parameterized tests.
 *
 * @see RetryFailedTest
 * @see RetryHandlerExtension
 * @see com.brandwatch.junit5.extensions.RetryTestExtension
 */
public class RetryTestTemplateInvocationContext extends GenericTestTemplateInvocationContext<RetryTestTemplateInvocationContext> {

    private final String displayName;

    public RetryTestTemplateInvocationContext(String displayName, TestTemplateIterator<RetryTestTemplateInvocationContext> iterator) {
        this(displayName, 0, iterator);
    }

    private RetryTestTemplateInvocationContext(String displayName, int failuresCount, TestTemplateIterator<RetryTestTemplateInvocationContext> iterator) {
        super(iterator, failuresCount);
        this.displayName = displayName;
    }

    @Override
    String getDisplayName() {
        return displayName;
    }

    @Override
    RetryTestTemplateInvocationContext getNextInvocationContext() {
        return new RetryTestTemplateInvocationContext(displayName, failuresCount + 1, iterator);
    }

    @Override
    RetryHandlerExtension<RetryTestTemplateInvocationContext> getRetryHandlerExtension() {
        return new RetryHandlerExtension<>(this);
    }
}
