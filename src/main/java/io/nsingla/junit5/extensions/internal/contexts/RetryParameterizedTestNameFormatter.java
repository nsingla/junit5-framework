package io.nsingla.junit5.extensions.internal.contexts;

import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.StringUtils;

import java.text.Format;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Copy of package-private JUnit class:
 * <p>
 *     {@code org.junit.jupiter.params.ParameterizedTestNameFormatter}
 * </p>
 * @since 5.0
 */
public class RetryParameterizedTestNameFormatter {

    private static final char ELLIPSIS = '\u2026';

    private final String pattern;
    private final String displayName;
    private final RetryParameterizedTestMethodContext methodContext;
    private final int argumentMaxLength;

    public RetryParameterizedTestNameFormatter(String pattern, String displayName, RetryParameterizedTestMethodContext methodContext, int argumentMaxLength) {
        this.pattern = pattern;
        this.displayName = displayName;
        this.methodContext = methodContext;
        this.argumentMaxLength = argumentMaxLength;
    }

    String format(int invocationIndex, Object... arguments) {
        try {
            return formatSafely(invocationIndex, arguments);
        } catch (Exception ex) {
            String message = "The display name pattern defined for the parameterized test is invalid. "
                + "See nested exception for further details.";
            throw new JUnitException(message, ex);
        }
    }

    private String formatSafely(int invocationIndex, Object[] arguments) {
        String pattern = prepareMessageFormatPattern(invocationIndex, arguments);
        MessageFormat format = new MessageFormat(pattern);
        Object[] humanReadableArguments = makeReadable(format, arguments);
        return format.format(humanReadableArguments);
    }

    private String prepareMessageFormatPattern(int invocationIndex, Object[] arguments) {
        String result = pattern
            .replace(DISPLAY_NAME_PLACEHOLDER, this.displayName)
            .replace(INDEX_PLACEHOLDER, String.valueOf(invocationIndex));

        if (result.contains(ARGUMENTS_WITH_NAMES_PLACEHOLDER)) {
            result = result.replace(ARGUMENTS_WITH_NAMES_PLACEHOLDER, argumentsWithNamesPattern(arguments));
        }

        if (result.contains(ARGUMENTS_PLACEHOLDER)) {
            result = result.replace(ARGUMENTS_PLACEHOLDER, argumentsPattern(arguments));
        }

        return result;
    }

    private String argumentsWithNamesPattern(Object[] arguments) {
        return IntStream.range(0, arguments.length)
            .mapToObj(index -> methodContext.getParameterName(index).map(name -> name + "=").orElse("") + "{" + index + "}")
            .collect(joining(", "));
    }

    private String argumentsPattern(Object[] arguments) {
        return IntStream.range(0, arguments.length)
            .mapToObj(index -> "{" + index + "}")
            .collect(joining(", "));
    }

    private Object[] makeReadable(MessageFormat format, Object[] arguments) {
        Format[] formats = format.getFormatsByArgumentIndex();
        Object[] result = Arrays.copyOf(arguments, Math.min(arguments.length, formats.length), Object[].class);
        for (int i = 0; i < result.length; i++) {
            if (formats[i] == null) {
                result[i] = truncateIfExceedsMaxLength(StringUtils.nullSafeToString(arguments[i]));
            }
        }
        return result;
    }

    private String truncateIfExceedsMaxLength(String argument) {
        if (argument != null && argument.length() > argumentMaxLength) {
            return argument.substring(0, argumentMaxLength - 1) + ELLIPSIS;
        }
        return argument;
    }
}
