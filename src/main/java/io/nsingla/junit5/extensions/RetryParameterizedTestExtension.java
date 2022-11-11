package io.nsingla.junit5.extensions;

import static java.util.Spliterators.spliteratorUnknownSize;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import io.nsingla.junit5.annotations.RetryFailedParameterizedTest;
import io.nsingla.junit5.extensions.internal.contexts.RetryParameterizedTestMethodContext;
import io.nsingla.junit5.extensions.internal.contexts.RetryParameterizedTestNameFormatter;
import io.nsingla.junit5.extensions.internal.contexts.RetryParameterizedTestTemplateInvocationContext;
import io.nsingla.junit5.extensions.internal.iterators.TestTemplateIterator;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Extension responsible of providing {@link TestTemplateInvocationContext} stream
 * for running and retrying parameterized tests.
 * @see RetryFailedParameterizedTest
 * @see RetryParameterizedTestTemplateInvocationContext
 *
 * @since 5.0
 */
public class RetryParameterizedTestExtension implements TestTemplateInvocationContextProvider {

    private static final String METHOD_CONTEXT_KEY = "context";
    private static final String ARGUMENT_MAX_LENGTH_KEY = "junit.jupiter.params.displayname.argument.maxlength";

    private final TestTemplateIterator<RetryParameterizedTestTemplateInvocationContext> iterator = new TestTemplateIterator<>();

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        if (!context.getTestMethod().isPresent()) {
            return false;
        }

        Method testMethod = context.getTestMethod().get();
        if (!isAnnotated(testMethod, RetryFailedParameterizedTest.class)) {
            return false;
        }
        RetryParameterizedTestMethodContext methodContext = new RetryParameterizedTestMethodContext(testMethod);
        Preconditions.condition(methodContext.hasPotentiallyValidSignature(),
            () -> String.format(
                "@ParameterizedTest method [%s] declares formal parameters in an invalid order: "
                    + "argument aggregators must be declared after any indexed arguments "
                    + "and before any arguments resolved by another ParameterResolver.",
                testMethod.toGenericString()));

        getStore(context).put(METHOD_CONTEXT_KEY, methodContext);
        return true;
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext extensionContext) {

        Method templateMethod = extensionContext.getRequiredTestMethod();
        String displayName = extensionContext.getDisplayName();
        RetryParameterizedTestMethodContext methodContext = getStore(extensionContext).get(METHOD_CONTEXT_KEY, RetryParameterizedTestMethodContext.class);
        int argumentMaxLength = extensionContext.getConfigurationParameter(ARGUMENT_MAX_LENGTH_KEY, Integer::parseInt).orElse(512);
        RetryParameterizedTestNameFormatter formatter = createNameFormatter(templateMethod, methodContext, displayName, argumentMaxLength);
        AtomicInteger invocationCount = new AtomicInteger(0);

        findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
            .stream()
            .map(ArgumentsSource::value)
            .map(this::instantiateArgumentsProvider)
            .map(provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider))
            .flatMap(provider -> arguments(provider, extensionContext))
            .map(Arguments::get)
            .map(arguments -> consumedArguments(arguments, methodContext))
            .map(arguments -> createInvocationContext(formatter, methodContext, arguments, invocationCount.incrementAndGet()))
            .onClose(() -> Preconditions.condition(invocationCount.get() > 0, "Configuration error: You must configure at least one set of arguments for this @ParameterizedTestWithRetry"))
            .forEach(iterator::add);

        iterator.setEndCountGoal(invocationCount.get());

        return StreamSupport.stream(spliteratorUnknownSize(iterator, Spliterator.NONNULL), false);
    }

    @SuppressWarnings("ConstantConditions")
    private ArgumentsProvider instantiateArgumentsProvider(Class<? extends ArgumentsProvider> clazz) {
        try {
            return ReflectionUtils.newInstance(clazz);
        } catch (Exception ex) {
            if (ex instanceof NoSuchMethodException) {
                String message = String.format("Failed to find a no-argument constructor for ArgumentsProvider [%s]. "
                        + "Please ensure that a no-argument constructor exists and "
                        + "that the class is either a top-level class or a static nested class",
                    clazz.getName());
                throw new JUnitException(message, ex);
            }
            throw ex;
        }
    }

    private ExtensionContext.Store getStore(ExtensionContext context) {
        return context.getStore(ExtensionContext.Namespace.create(RetryParameterizedTestExtension.class, context.getRequiredTestMethod()));
    }

    private RetryParameterizedTestTemplateInvocationContext createInvocationContext(RetryParameterizedTestNameFormatter formatter, RetryParameterizedTestMethodContext methodContext,
                                                                                    Object[] arguments, int invocationIndex) {
        return new RetryParameterizedTestTemplateInvocationContext(formatter, methodContext, arguments, invocationIndex, iterator);
    }

    private RetryParameterizedTestNameFormatter createNameFormatter(Method templateMethod, RetryParameterizedTestMethodContext methodContext,
                                                                    String displayName, int argumentMaxLength) {

        Optional<RetryFailedParameterizedTest> parameterizedTest = findAnnotation(templateMethod, RetryFailedParameterizedTest.class);

        Preconditions.condition(parameterizedTest.isPresent(),
            () -> String.format("Configuration error: @ParameterizedTestWithRetry annotation not found on method [%s]", templateMethod));

        // noinspection OptionalGetWithoutIsPresent
        String pattern = Preconditions.notBlank(parameterizedTest.get().name().trim(),
            () -> String.format("Configuration error: @ParameterizedTestWithRetry on method [%s] must be declared with a non-empty name.", templateMethod));
        return new RetryParameterizedTestNameFormatter(pattern, displayName, methodContext, argumentMaxLength);
    }

    protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context) {
        try {
            return provider.provideArguments(context);
        } catch (Exception e) {
            throw ExceptionUtils.throwAsUncheckedException(e);
        }
    }

    private Object[] consumedArguments(Object[] arguments, RetryParameterizedTestMethodContext methodContext) {
        int parameterCount = methodContext.getParameterCount();
        return methodContext.hasAggregator()
            ? arguments
            : arguments.length > parameterCount
                ? Arrays.copyOf(arguments, parameterCount)
                : arguments;
    }
}
