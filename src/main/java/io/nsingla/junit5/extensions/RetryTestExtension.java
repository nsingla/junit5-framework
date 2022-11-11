package io.nsingla.junit5.extensions;

import static java.util.Spliterators.spliteratorUnknownSize;

import io.nsingla.junit5.annotations.RetryFailedTest;
import io.nsingla.junit5.extensions.internal.contexts.RetryTestTemplateInvocationContext;
import io.nsingla.junit5.extensions.internal.iterators.TestTemplateIterator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;

import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Extension responsible of providing {@link TestTemplateInvocationContext} stream
 * for running and retrying non-parameterized tests.
 *
 * @see RetryFailedTest
 * @see RetryTestTemplateInvocationContext
 */
public class RetryTestExtension implements TestTemplateInvocationContextProvider {

    private final TestTemplateIterator<RetryTestTemplateInvocationContext> iterator = new TestTemplateIterator<>();

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        if (!context.getTestMethod().isPresent()) {
            return false;
        }
        return AnnotationUtils.isAnnotated(context.getTestMethod(), RetryFailedTest.class);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        iterator.add(new RetryTestTemplateInvocationContext(context.getDisplayName(), iterator));
        return StreamSupport.stream(spliteratorUnknownSize(iterator, Spliterator.NONNULL), false);
    }
}
