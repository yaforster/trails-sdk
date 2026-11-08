package io.github.yaforster.trails.adapter.api;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.run.TestExecutionEventFacade;
import io.github.yaforster.trails.adapter.api.asyncapi.run.TestExecutionEventPublisherImpl;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionLinkFactory;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import io.github.yaforster.trails.app.services.TimeSource;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class TestExecutionEventHubTest {

    @Test
    void shouldRetainOnlyTerminalEventsForReplay() {
        TestExecutionEventHub eventHub = eventHub(0L);
        UUID executionId = UUID.randomUUID();

        eventHub.publishStarted(executionId, "started");
        eventHub.publishFailed(executionId, "failed");

        assertThat(eventHub.terminalEvent(executionId)).isEqualTo("failed");
        assertThat(eventHub.terminalEventCount()).isOne();
    }

    @Test
    void shouldExpireRetainedTerminalEventAfterReplayWindow() {
        AtomicLong clock = new AtomicLong();
        TestExecutionEventHub eventHub = new TestExecutionEventHub((TimeSource) clock::get);
        UUID executionId = UUID.randomUUID();
        eventHub.publishCompleted(executionId, "completed");
        clock.set(10 * 60 * 1000L);

        assertThat(eventHub.terminalEvent(executionId)).isNull();
    }

    @Test
    void shouldPublishPersistedFailedOutcomeAsTerminalFailureEvent() {
        TestExecutionEventHub eventHub = eventHub(0L);
        TestExecutionEventPublisherImpl publisher = new TestExecutionEventPublisherImpl(
                new TestExecutionEventFacade(new TestExecutionLinkFactory()), eventHub);
        UUID executionId = UUID.randomUUID();
        TestRunTerminalOutcome outcome = new TestRunTerminalOutcome(
                new PersistedTestRunResult(99L, 1L, 2L, 3L, new Timestamp(1L), ResultIndicator.FAILURE, "Plan"),
                TestRunTerminalStatus.FAILED);

        publisher.publishTerminal(executionId, outcome);

        assertThat(eventHub.terminalEvent(executionId)).isInstanceOf(TestExecutionFailedEventAsyncDTO.class);
    }

    private TestExecutionEventHub eventHub(long now) {
        return new TestExecutionEventHub(() -> now);
    }

}
