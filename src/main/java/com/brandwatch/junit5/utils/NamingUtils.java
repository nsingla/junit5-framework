package com.brandwatch.junit5.utils;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtensionContext;

public class NamingUtils {

    public static String getTestName(TestInfo testInfo) {
        return testInfo.getTestClass().get().getSimpleName() + "." + testInfo.getDisplayName();
    }

    public static String getTestName(ExtensionContext extensionContext) {
        return extensionContext.getTestClass().get().getSimpleName() + "." + extensionContext.getDisplayName();
    }
}
