package com.brandwatch.junit5.extensions.internal.contexts;

import com.brandwatch.junit5.annotations.RetryFailedParameterizedTest;
import com.brandwatch.junit5.extensions.RetryHandlerExtension;
import com.brandwatch.junit5.extensions.internal.iterators.TestTemplateIterator;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link TestTemplateInvocationContext} that's holding necessary information
 * and providing additional extensions for handling failures and retrying parameterized tests.
 *
 * @see RetryFailedParameterizedTest
 * @see RetryHandlerExtension
 * @see com.brandwatch.junit5.extensions.RetryParameterizedTestExtension
 */
public class RetryParameterizedTestTemplateInvocationContext extends GenericTestTemplateInvocationContext<RetryParameterizedTestTemplateInvocationContext> {

    private final RetryParameterizedTestNameFormatter formatter;
    private final RetryParameterizedTestMethodContext methodContext;
    private final Object[] arguments;
    private final int invocationIndex;

    public RetryParameterizedTestTemplateInvocationContext(RetryParameterizedTestNameFormatter formatter, RetryParameterizedTestMethodContext methodContext,
                                                           Object[] arguments, int invocationIndex,
                                                           TestTemplateIterator<RetryParameterizedTestTemplateInvocationContext> iterator) {
        this(formatter, methodContext, arguments, 0, invocationIndex, iterator);
    }

    private RetryParameterizedTestTemplateInvocationContext(RetryParameterizedTestNameFormatter formatter, RetryParameterizedTestMethodContext methodContext,
                                                            Object[] arguments, int failuresCount, int invocationIndex,
                                                            TestTemplateIterator<RetryParameterizedTestTemplateInvocationContext> iterator) {
        super(iterator, failuresCount);
        this.formatter = formatter;
        this.methodContext = methodContext;
        this.arguments = arguments;
        this.invocationIndex = invocationIndex;
    }

    @Override
    String getDisplayName() {
        return formatter.format(invocationIndex, arguments);
    }

    @Override
    RetryParameterizedTestTemplateInvocationContext getNextInvocationContext() {
        return new RetryParameterizedTestTemplateInvocationContext(formatter, methodContext, arguments, failuresCount + 1, invocationIndex, iterator);
    }

    @Override
    RetryHandlerExtension<RetryParameterizedTestTemplateInvocationContext> getRetryHandlerExtension() {
        return new RetryHandlerExtension<>(this);
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        List<Extension> extensions = new ArrayList<>();
        extensions.add(new RetryParameterizedTestParameterResolver(this.methodContext, this.arguments));
        extensions.addAll(super.getAdditionalExtensions());
        return extensions;
    }
}
