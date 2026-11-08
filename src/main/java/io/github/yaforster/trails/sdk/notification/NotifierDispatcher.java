package io.github.yaforster.trails.sdk.notification;

import org.jspecify.annotations.NonNull;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delivers terminal notifications asynchronously without allowing one notifier to affect another.
 */
public final class NotifierDispatcher implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(NotifierDispatcher.class.getName());
    private final List<Notifier> notifiers;
    private final ThreadPoolExecutor executor;
    private final Duration shutdownTimeout;

    public NotifierDispatcher(List<Notifier> notifiers, NotifierDispatchLimits limits) {
        Objects.requireNonNull(limits, "Notifier dispatch limits are required");
        this.notifiers = List.copyOf(Objects.requireNonNull(notifiers, "Notifiers are required"))
                             .stream()
                             .sorted(Comparator.comparing(notifier -> notifier.adapterId()
                                                                              .value()))
                             .toList();
        this.shutdownTimeout = limits.shutdownTimeout();
        this.executor = createExecutor(limits);
    }

    /**
     * Schedules every notifier in the startup snapshot. Queue rejection is isolated, logged without notification
     * content, and reported to the caller; accepted notifier calls remain asynchronous.
     */
    public NotifierDispatchResult dispatch(TerminalRunNotification notification) {
        Objects.requireNonNull(notification, "Terminal notification is required");
        int accepted = 0;
        int rejected = 0;
        for (Notifier notifier : notifiers) {
            if (schedule(notification, notifier)) {
                accepted++;
            } else {
                rejected++;
            }
        }
        return new NotifierDispatchResult(accepted, rejected);
    }

    @Override
    public void close() {
        executor.shutdown();
        awaitTermination();
    }

    private ThreadPoolExecutor createExecutor(NotifierDispatchLimits limits) {
        return new ThreadPoolExecutor(limits.workerCount(), limits.workerCount(), 0, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(limits.queueCapacity()), new NotifierThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());
    }

    private boolean schedule(TerminalRunNotification notification, Notifier notifier) {
        try {
            executor.execute(() -> notifyIsolated(notification, notifier));
            return true;
        } catch (RejectedExecutionException exception) {
            log(notification, notifier, exception);
            return false;
        }
    }

    private void notifyIsolated(TerminalRunNotification notification, Notifier notifier) {
        try {
            notifier.notify(notification);
        } catch (RuntimeException exception) {
            log(notification, notifier, exception);
        }
    }

    private void awaitTermination() {
        try {
            if (!executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException exception) {
            executor.shutdownNow();
            Thread.currentThread()
                  .interrupt();
        }
    }

    private static void log(TerminalRunNotification notification, Notifier notifier, RuntimeException exception) {
        LOGGER.log(Level.WARNING, "Terminal notifier failed: executionId={0}, testRunId={1}, notifier={2}, failure={3}",
                new Object[]{notification.executionId(), notification.testRunId(), notifier.adapterId()
                                                                                           .value(),
                        exception.getClass()
                                 .getName()});
    }

    private static final class NotifierThreadFactory implements ThreadFactory {

        private final AtomicLong threadNumber = new AtomicLong();

        @Override
        public Thread newThread(@NonNull Runnable task) {
            return Thread.ofPlatform()
                         .daemon()
                         .name("trails-notifier-" + threadNumber.incrementAndGet())
                         .unstarted(task);
        }

    }

}
