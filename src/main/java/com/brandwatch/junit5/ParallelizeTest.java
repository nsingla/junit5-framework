package com.brandwatch.junit5;

import com.brandwatch.junit5.utils.NamingUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.slf4j.MDC;

@ExtendWith(CutomLogger.class)
@Execution(ExecutionMode.CONCURRENT)
public class ParallelizeTest {

    protected String methodName = null;

    @BeforeEach
    public void addTestNameToMDC(TestInfo testInfo) {
        MDC.put("methodName", NamingUtils.getTestName(testInfo));
        methodName = testInfo.getDisplayName();
    }

    @AfterEach
    public void cleanMDC() {
        MDC.remove("methodName");
    }
}
