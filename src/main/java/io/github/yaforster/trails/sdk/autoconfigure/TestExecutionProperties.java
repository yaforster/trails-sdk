package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.host.execution.TestExecutionLimits;
import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Host-owned configuration for bounded asynchronous test execution.
 *
 * @param maximumConcurrentRuns
 *            maximum simultaneous admitted runs
 * @param runQueueCapacity
 *            queued runs; zero performs direct hand-off
 * @param maximumConcurrentTestSets
 *            maximum simultaneous browser test sets
 * @param shutdownTimeout
 *            bounded graceful shutdown duration
 */
@ConfigurationProperties("trails.execution")
public record TestExecutionProperties(Integer maximumConcurrentRuns, Integer runQueueCapacity,
        Integer maximumConcurrentTestSets, Duration shutdownTimeout) {

    private static final int DEFAULT_MAXIMUM_CONCURRENT_RUNS = 16;
    private static final int DEFAULT_RUN_QUEUE_CAPACITY = 64;
    private static final int DEFAULT_MAXIMUM_CONCURRENT_TEST_SETS = 16;
    private static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);

    public TestExecutionProperties {
        maximumConcurrentRuns = Optional.ofNullable(maximumConcurrentRuns)
                                        .orElse(DEFAULT_MAXIMUM_CONCURRENT_RUNS);
        runQueueCapacity = Optional.ofNullable(runQueueCapacity)
                                   .orElse(DEFAULT_RUN_QUEUE_CAPACITY);
        maximumConcurrentTestSets = Optional.ofNullable(maximumConcurrentTestSets)
                                            .orElse(DEFAULT_MAXIMUM_CONCURRENT_TEST_SETS);
        shutdownTimeout = Optional.ofNullable(shutdownTimeout)
                                  .orElse(DEFAULT_SHUTDOWN_TIMEOUT);
    }

    TestExecutionLimits limits() {
        return new TestExecutionLimits(maximumConcurrentRuns, runQueueCapacity, maximumConcurrentTestSets,
                shutdownTimeout);
    }

}
