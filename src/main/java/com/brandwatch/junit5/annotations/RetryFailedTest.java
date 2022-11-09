package com.brandwatch.junit5.annotations;

import com.brandwatch.junit5.extensions.RetryTestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@code @TestWithRetry} is an extended version of {@link Test},
 * capable of marking failed tests as skipped and re-running them again.
 * <p>
 *     The maximum number of retries is customizable via {@code retryCount} environment property.
 * </p>
 *
 * @see Test
 */
@TestTemplate
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(RetryTestExtension.class)
public @interface RetryFailedTest {

}
