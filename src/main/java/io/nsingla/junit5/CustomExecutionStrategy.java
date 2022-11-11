package io.nsingla.junit5;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom execution strategy that sets the number of parallel runs to the value of the system property: {@code threadCount}.
 *
 * <p> Value must be an integer; defaults to {@code 1}.
 */
public class CustomExecutionStrategy implements ParallelExecutionConfigurationStrategy {

    private static final Logger logger = LoggerFactory.getLogger(CustomExecutionStrategy.class);
    private static final int THREAD_COUNT = Integer.parseInt(System.getProperty("threadCount", "1"));

    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        // Read threads from configuration parameters if set
        // Mostly used for running tests programmatically
        Integer threadCount = configurationParameters.get("threads")
            .map(Integer::parseInt)
            .orElse(THREAD_COUNT);

        logger.debug("Tests will run on {} thread(s).", threadCount);

        return new ParallelExecutionConfiguration() {

            @Override
            public int getParallelism() {
                return threadCount;
            }

            @Override
            public int getMinimumRunnable() {
                return threadCount;
            }

            @Override
            public int getMaxPoolSize() {
                return threadCount + 256;
            }

            @Override
            public int getKeepAliveSeconds() {
                return 30;
            }

            @Override
            public int getCorePoolSize() {
                return threadCount;
            }
        };
    }
}
