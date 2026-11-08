package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Publishes SDK execution transitions through the AsyncAPI SSE transport. */
public final class TestExecutionEventPublisherImpl implements TestExecutionEventPublisher {

    private final TestExecutionEventFacade events;
    private final TestExecutionEventHub eventHub;

    public TestExecutionEventPublisherImpl(TestExecutionEventFacade events, TestExecutionEventHub eventHub) {
        this.events = Objects.requireNonNull(events, "Test execution event facade is required");
        this.eventHub = Objects.requireNonNull(eventHub, "Test execution event hub is required");
    }

    @Override
    public void publishStarted(UUID executionId) {
        eventHub.publishStarted(executionId, events.startedEvent(executionId));
    }

    @Override
    public Optional<URI> publishTerminal(UUID executionId, TestRunTerminalOutcome outcome) {
        TestExecutionEventFacade.TerminalEvent event = events.terminalEvent(executionId, outcome);
        if (outcome.status() == TestRunTerminalStatus.COMPLETED) {
            eventHub.publishCompleted(executionId, event.payload());
        } else {
            eventHub.publishFailed(executionId, event.payload());
        }
        return Optional.of(event.resultUri());
    }

    @Override
    public void publishUnpersistedFailure(UUID executionId) {
        eventHub.publishFailed(executionId, events.unpersistedFailureEvent(executionId));
    }

}
