package com.brandwatch.junit5.annotations;

import static org.apiguardian.api.API.Status.STABLE;

import com.brandwatch.junit5.extensions.RetryParameterizedTestExtension;
import org.apiguardian.api.API;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @ParameterizedTestWithRetry} is an extended version of {@link ParameterizedTest},
 * capable of marking failed tests as skipped and re-running them again.
 * <p>
 * The maximum number of retries is customizable via {@code retryCount} environment property.
 * </p>
 *
 * @see ParameterizedTest
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(status = STABLE, since = "5.7")
@TestTemplate
@ExtendWith(RetryParameterizedTestExtension.class)
public @interface RetryFailedParameterizedTest {

    /**
     * Placeholder for the {@linkplain org.junit.jupiter.api.TestInfo#getDisplayName
     * display name} of a {@code @ParameterizedTest} method: <code>{displayName}</code>
     *
     * @see #name
     * @since 5.3
     */
    String DISPLAY_NAME_PLACEHOLDER = "{displayName}";

    /**
     * Placeholder for the current invocation index of a {@code @ParameterizedTest}
     * method (1-based): <code>{index}</code>
     *
     * @see #name
     * @since 5.3
     */
    String INDEX_PLACEHOLDER = "{index}";

    /**
     * Placeholder for the complete, comma-separated arguments list of the
     * current invocation of a {@code @ParameterizedTest} method:
     * <code>{arguments}</code>
     *
     * @see #name
     * @since 5.3
     */
    String ARGUMENTS_PLACEHOLDER = "{arguments}";

    /**
     * Placeholder for the complete, comma-separated named arguments list
     * of the current invocation of a {@code @ParameterizedTest} method:
     * <code>{argumentsWithNames}</code>
     *
     * @see #name
     * @since 5.6
     */
    String ARGUMENTS_WITH_NAMES_PLACEHOLDER = "{argumentsWithNames}";

    /**
     * Default display name pattern for the current invocation of a
     * {@code @ParameterizedTest} method: {@value}
     *
     * <p>Note that the default pattern does <em>not</em> include the
     * {@linkplain #DISPLAY_NAME_PLACEHOLDER display name} of the
     * {@code @ParameterizedTest} method.
     *
     * @see #name
     * @see #DISPLAY_NAME_PLACEHOLDER
     * @see #INDEX_PLACEHOLDER
     * @see #ARGUMENTS_WITH_NAMES_PLACEHOLDER
     * @since 5.3
     */
    String DEFAULT_DISPLAY_NAME = "[" + INDEX_PLACEHOLDER + "] " + ARGUMENTS_WITH_NAMES_PLACEHOLDER;

    /**
     * The display name to be used for individual invocations of the
     * parameterized test; never blank or consisting solely of whitespace.
     *
     * Defaults to {@link #DEFAULT_DISPLAY_NAME}.
     *
     * Supported placeholders
     * <ul>
     * <li>{@link #DISPLAY_NAME_PLACEHOLDER}</li>
     * <li>{@link #INDEX_PLACEHOLDER}</li>
     * <li>{@link #ARGUMENTS_PLACEHOLDER}</li>
     * <li><code>{0}</code>, <code>{1}</code>, etc.: an individual argument (0-based)</li>
     * </ul>
     *
     * <p>For the latter, you may use {@link java.text.MessageFormat} patterns
     * to customize formatting. Please note that the original arguments are
     * passed when formatting, regardless of any implicit or explicit argument
     * conversions.
     *
     * @return Display Name of the test
     *
     * @see java.text.MessageFormat
     */
    String name() default DEFAULT_DISPLAY_NAME;

}
