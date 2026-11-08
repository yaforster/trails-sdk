package io.github.yaforster.trails.sdk.host.execution;

import java.time.Duration;
import java.util.Objects;

/**
 * Explicit bounds for host-owned test-run and browser-test-set work.
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
public record TestExecutionLimits(int maximumConcurrentRuns, int runQueueCapacity, int maximumConcurrentTestSets,
        Duration shutdownTimeout) {

    public TestExecutionLimits {
        if (maximumConcurrentRuns <= 0) {
            throw new IllegalArgumentException("Maximum concurrent test runs must be positive");
        }
        if (runQueueCapacity < 0) {
            throw new IllegalArgumentException("Test run queue capacity cannot be negative");
        }
        if (maximumConcurrentTestSets <= 0) {
            throw new IllegalArgumentException("Maximum concurrent test sets must be positive");
        }
        Objects.requireNonNull(shutdownTimeout, "Test execution shutdown timeout is required");
        if (shutdownTimeout.isNegative() || shutdownTimeout.isZero() || shutdownTimeout.toMillis() == 0) {
            throw new IllegalArgumentException("Test execution shutdown timeout must be at least one millisecond");
        }
    }

}
