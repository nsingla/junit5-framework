package io.nsingla.junit5.extensions.internal.contexts;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * Copy of package-private JUnit class:
 * <p>
 *     {@code org.junit.jupiter.params.ParameterizedTestParameterResolver}
 * </p>
 * @since 5.0
 */
class RetryParameterizedTestParameterResolver implements ParameterResolver {

    private final RetryParameterizedTestMethodContext methodContext;
    private final Object[] arguments;

    RetryParameterizedTestParameterResolver(RetryParameterizedTestMethodContext methodContext, Object[] arguments) {
        this.methodContext = methodContext;
        this.arguments = arguments;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Executable declaringExecutable = parameterContext.getDeclaringExecutable();
        Method testMethod = extensionContext.getTestMethod().orElse(null);
        int parameterIndex = parameterContext.getIndex();

        // Not a @ParameterizedTest method?
        if (!declaringExecutable.equals(testMethod)) {
            return false;
        }

        // Current parameter is an aggregator?
        if (this.methodContext.isAggregator(parameterIndex)) {
            return true;
        }

        // Ensure that the current parameter is declared before aggregators.
        // Otherwise, a different ParameterResolver should handle it.
        if (this.methodContext.hasAggregator()) {
            return parameterIndex < this.methodContext.indexOfFirstAggregator();
        }

        // Else fallback to behavior for parameterized test methods without aggregators.
        return parameterIndex < this.arguments.length;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return this.methodContext.resolve(parameterContext, this.arguments);
    }
}
