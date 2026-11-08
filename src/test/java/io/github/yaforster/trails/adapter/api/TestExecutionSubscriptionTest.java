package io.github.yaforster.trails.adapter.api;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import static org.assertj.core.api.Assertions.*;

class TestExecutionSubscriptionTest {
    @Test
    void shouldDeliverTerminalExactlyOnce_whenSubscriptionRacesWithCompletion() throws Exception {
        TestExecutionEventHub hub = new TestExecutionEventHub(() -> 0L);
        try (ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int attempt = 0; attempt < 100; attempt++) {
                UUID execution = UUID.randomUUID();
                RecordingEmitter subscriber = new RecordingEmitter();
                CyclicBarrier start = new CyclicBarrier(2);
                Future<?> subscribe = workers.submit(() -> {
                    await(start);
                    hub.subscribe(execution, subscriber);
                });
                Future<?> complete = workers.submit(() -> {
                    await(start);
                    hub.publishCompleted(execution, "done");
                });
                subscribe.get(5, TimeUnit.SECONDS);
                complete.get(5, TimeUnit.SECONDS);
                assertThat(subscriber.deliveries.get()).isOne();
            }
        }
    }

    @Test
    void shouldAllowTerminalReplay_whenAnExistingSubscriberBlocksDelivery() throws Exception {
        TestExecutionEventHub hub = new TestExecutionEventHub(() -> 0L);
        UUID execution = UUID.randomUUID();
        CountDownLatch sending = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        SseEmitter slow = new SseEmitter() {
            @Override
            public void send(SseEventBuilder event) throws IOException {
                sending.countDown();
                try {
                    release.await();
                } catch (InterruptedException failure) {
                    throw new IOException(failure);
                }
            }
        };
        hub.subscribe(execution, slow);
        try (ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> terminal = workers.submit(() -> hub.publishCompleted(execution, "done"));
            try {
                if (!sending.await(5, TimeUnit.SECONDS))
                    throw new AssertionError("Delivery did not start");
                RecordingEmitter replay = new RecordingEmitter();
                workers.submit(() -> hub.subscribe(execution, replay))
                       .get(5, TimeUnit.SECONDS);
                assertThat(replay.deliveries.get()).isOne();
            } finally {
                release.countDown();
            }
            terminal.get(5, TimeUnit.SECONDS);
        }
    }

    private static void await(CyclicBarrier barrier) {
        try {
            barrier.await(5, TimeUnit.SECONDS);
        } catch (Exception failure) {
            throw new AssertionError(failure);
        }
    }

    private static final class RecordingEmitter extends SseEmitter {
        private final AtomicInteger deliveries = new AtomicInteger();
        @Override
        public void send(SseEventBuilder event) {
            deliveries.incrementAndGet();
        }
    }
}
