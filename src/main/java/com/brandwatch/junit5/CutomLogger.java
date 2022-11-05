package com.brandwatch.junit5;

import com.brandwatch.junit5.utils.NamingUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Optional;

public class CutomLogger implements org.junit.jupiter.api.extension.TestWatcher {

    private static final Logger logger = LoggerFactory.getLogger(CutomLogger.class);

    @Override
    public void testDisabled(ExtensionContext extensionContext, Optional<String> optional) {
        logger.info("Test {} is disabled run.", NamingUtils.getTestName(extensionContext));
    }

    @Override
    public void testSuccessful(ExtensionContext extensionContext) {
        logger.info("Test {} succesfully run.", NamingUtils.getTestName(extensionContext));
        MDC.remove("methodName");
    }

    @Override
    public void testAborted(ExtensionContext extensionContext, Throwable throwable) {
        logger.info("Test {} aborted.", NamingUtils.getTestName(extensionContext));
        MDC.remove("methodName");
    }

    @Override
    public void testFailed(ExtensionContext extensionContext, Throwable throwable) {
        String testName = NamingUtils.getTestName(extensionContext);
        MDC.put("methodName", testName);
        logger.info("Test {} failed.", testName);
        MDC.remove("methodName");
    }
}