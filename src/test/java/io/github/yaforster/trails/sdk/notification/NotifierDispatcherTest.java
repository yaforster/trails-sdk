package io.github.yaforster.trails.sdk.notification;

import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class NotifierDispatcherTest {

    @Test
    void shouldDispatchInStableNotifierIdentifierOrder() throws InterruptedException {
        List<String> invocations = new CopyOnWriteArrayList<>();
        CountDownLatch completed = new CountDownLatch(2);
        try (NotifierDispatcher dispatcher = new NotifierDispatcher(
                List.of(notifier("zulu", ignored -> recordInvocation(invocations, completed, "zulu")),
                        notifier("alpha", ignored -> recordInvocation(invocations, completed, "alpha"))),
                limits(1, 2))) {
            NotifierDispatchResult result = dispatcher.dispatch(notification());

            assertThat(result).isEqualTo(new NotifierDispatchResult(2, 0));
            assertThat(completed.await(1, TimeUnit.SECONDS)).isTrue();
        }

        assertThat(invocations).containsExactly("alpha", "zulu");
    }

    @Test
    void shouldContinueWithOtherNotifiers_whenOneNotifierFails() throws InterruptedException {
        CountDownLatch completed = new CountDownLatch(1);
        try (NotifierDispatcher dispatcher = new NotifierDispatcher(List.of(notifier("failing", ignored -> {
            throw new IllegalStateException("notifier failure");
        }), notifier("working", ignored -> completed.countDown())), limits(1, 2))) {
            dispatcher.dispatch(notification());

            assertThat(completed.await(1, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void shouldRejectOnlyTheTaskThatExceedsTheBoundedQueue() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Notifier blockingNotifier = notifier("blocking", ignored -> awaitRelease(started, release));
        try (NotifierDispatcher dispatcher = new NotifierDispatcher(List.of(blockingNotifier), limits(1, 1))) {
            assertThat(dispatcher.dispatch(notification())).isEqualTo(new NotifierDispatchResult(1, 0));
            assertThat(started.await(1, TimeUnit.SECONDS)).isTrue();
            assertThat(dispatcher.dispatch(notification())).isEqualTo(new NotifierDispatchResult(1, 0));
            assertThat(dispatcher.dispatch(notification())).isEqualTo(new NotifierDispatchResult(0, 1));
            release.countDown();
        }
    }

    @Test
    void shouldRejectNonPositiveDispatchLimits() {
        assertThatIllegalArgumentException().isThrownBy(() -> new NotifierDispatchLimits(0, 1, Duration.ofSeconds(1)));
    }

    private static NotifierDispatchLimits limits(int workerCount, int queueCapacity) {
        return new NotifierDispatchLimits(workerCount, queueCapacity, Duration.ofSeconds(1));
    }

    private static TerminalRunNotification notification() {
        return new TerminalRunNotification(UUID.randomUUID(), 1L, TestRunTerminalStatus.COMPLETED,
                io.github.yaforster.trails.core.data.ResultIndicator.SUCCESS, Optional.empty(), Optional.empty());
    }

    private static Notifier notifier(String adapterId, Consumer<TerminalRunNotification> consumer) {
        return new Notifier() {

            @Override
            public AdapterId adapterId() {
                return new AdapterId(adapterId);
            }

            @Override
            public void notify(TerminalRunNotification notification) {
                consumer.accept(notification);
            }

        };
    }

    private static void recordInvocation(List<String> invocations, CountDownLatch completed, String adapterId) {
        invocations.add(adapterId);
        completed.countDown();
    }

    private static void awaitRelease(CountDownLatch started, CountDownLatch release) {
        started.countDown();
        try {
            release.await();
        } catch (InterruptedException exception) {
            Thread.currentThread()
                  .interrupt();
        }
    }

}
