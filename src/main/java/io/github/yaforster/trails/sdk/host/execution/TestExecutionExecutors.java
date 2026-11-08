package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.core.test.Browser;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Future;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns the bounded worker resources used by the test-run host. */
final class TestExecutionExecutors implements AutoCloseable {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestExecutionExecutors.class);
    private final ExecutorService runs;
    private final ExecutorService testSets;
    private final Duration shutdownTimeout;

    private TestExecutionExecutors(ExecutorService runs, ExecutorService testSets, Duration shutdownTimeout) {
        this.runs = runs;
        this.testSets = testSets;
        this.shutdownTimeout = shutdownTimeout;
    }

    static TestExecutionExecutors create(TestExecutionLimits limits) {
        Objects.requireNonNull(limits, "Test execution limits are required");
        ExecutorService runs = executor(limits.maximumConcurrentRuns(), limits.runQueueCapacity(), "trails-test-run-");
        int testSetQueueCapacity = Math.multiplyExact(limits.maximumConcurrentRuns(), Browser.values().length);
        ExecutorService testSets = executor(limits.maximumConcurrentTestSets(), testSetQueueCapacity,
                "trails-test-set-");
        return new TestExecutionExecutors(runs, testSets, limits.shutdownTimeout());
    }

    ExecutorService runs() {
        return runs;
    }

    ExecutorService testSets() {
        return testSets;
    }

    @Override
    public void close() {
        shutdown(runs);
        shutdown(testSets);
    }

    private static ExecutorService executor(int workerCount, int queueCapacity, String threadPrefix) {
        return new ThreadPoolExecutor(workerCount, workerCount, 0, TimeUnit.MILLISECONDS, queue(queueCapacity),
                new NamedThreadFactory(threadPrefix), new ThreadPoolExecutor.AbortPolicy());
    }

    private static BlockingQueue<Runnable> queue(int queueCapacity) {
        if (queueCapacity == 0) {
            return new SynchronousQueue<>();
        }
        return new ArrayBlockingQueue<>(queueCapacity);
    }

    private void shutdown(ExecutorService executor) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS)) {
                cancelQueuedTasks(executor);
                executor.awaitTermination(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException exception) {
            cancelQueuedTasks(executor);
            Thread.currentThread()
                  .interrupt();
        }
    }

    private void cancelQueuedTasks(ExecutorService executor) {
        for (Runnable task : executor.shutdownNow()) {
            try {
                if (task instanceof AdmittedTestRun run) {
                    run.cancelBeforeStart();
                } else if (task instanceof Future<?> future) {
                    future.cancel(false);
                }
            } catch (RuntimeException exception) {
                // One lifecycle callback must not prevent terminal handling of the remaining admitted runs.
                LOGGER.warn("Queued execution cancellation failed ({})", exception.getClass()
                                                                                  .getName());
            }
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {

        private final AtomicLong threadNumber = new AtomicLong();
        private final String threadPrefix;

        private NamedThreadFactory(String threadPrefix) {
            this.threadPrefix = threadPrefix;
        }

        @Override
        public Thread newThread(@NonNull Runnable task) {
            return Thread.ofPlatform()
                         .name(threadPrefix + threadNumber.incrementAndGet())
                         .unstarted(task);
        }

    }

}
