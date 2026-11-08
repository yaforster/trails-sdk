package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.notification.NotifierDispatchLimits;
import java.time.Duration;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Host-owned configuration for bounded asynchronous notification delivery.
 *
 * @param workerCount
 *            notifier worker count
 * @param queueCapacity
 *            queued notifier calls
 * @param shutdownTimeout
 *            bounded graceful shutdown duration
 */
@ConfigurationProperties("trails.notification.dispatch")
public record NotifierDispatchProperties(Integer workerCount, Integer queueCapacity, Duration shutdownTimeout) {

    private static final int DEFAULT_WORKER_COUNT = 4;
    private static final int DEFAULT_QUEUE_CAPACITY = 100;
    private static final Duration DEFAULT_SHUTDOWN_TIMEOUT = Duration.ofSeconds(30);

    public NotifierDispatchProperties {
        workerCount = Optional.ofNullable(workerCount)
                              .orElse(DEFAULT_WORKER_COUNT);
        queueCapacity = Optional.ofNullable(queueCapacity)
                                .orElse(DEFAULT_QUEUE_CAPACITY);
        shutdownTimeout = Optional.ofNullable(shutdownTimeout)
                                  .orElse(DEFAULT_SHUTDOWN_TIMEOUT);
    }

    NotifierDispatchLimits limits() {
        return new NotifierDispatchLimits(workerCount, queueCapacity, shutdownTimeout);
    }

}
