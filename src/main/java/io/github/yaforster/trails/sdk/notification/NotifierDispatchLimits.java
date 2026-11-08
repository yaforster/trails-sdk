package io.github.yaforster.trails.sdk.notification;

import java.time.Duration;
import java.util.Objects;

/** Explicit bounds for asynchronous terminal-result notification work. */
public record NotifierDispatchLimits(int workerCount, int queueCapacity, Duration shutdownTimeout) {

    public NotifierDispatchLimits {
        if (workerCount <= 0) {
            throw new IllegalArgumentException("Notifier worker count must be positive");
        }
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("Notifier queue capacity must be positive");
        }
        Objects.requireNonNull(shutdownTimeout, "Notifier shutdown timeout is required");
        if (shutdownTimeout.isNegative() || shutdownTimeout.isZero() || shutdownTimeout.toMillis() == 0) {
            throw new IllegalArgumentException("Notifier shutdown timeout must be at least one millisecond");
        }
    }

}
