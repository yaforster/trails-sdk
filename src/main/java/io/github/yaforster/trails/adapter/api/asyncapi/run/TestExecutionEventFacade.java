package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionStartedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionLinkFactory;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;

import java.net.URI;
import java.util.Objects;
import java.util.UUID;

/** Maps execution lifecycle transitions to the unchanged AsyncAPI event shapes. */
public final class TestExecutionEventFacade {

    private final TestExecutionLinkFactory links;

    public TestExecutionEventFacade(TestExecutionLinkFactory links) {
        this.links = Objects.requireNonNull(links, "Test execution links are required");
    }

    public TestExecutionStartedEventAsyncDTO startedEvent(UUID executionId) {
        return new TestExecutionStartedEventAsyncDTO(executionId.toString(),
                TestExecutionStartedEventAsyncDTO.Status.RUNNING, "Test execution started",
                links.startedLinks(executionId));
    }

    public TerminalEvent terminalEvent(UUID executionId, TestRunTerminalOutcome outcome) {
        Objects.requireNonNull(outcome, "Test run terminal outcome is required");
        TestExecutionLinkFactory.TerminalLinks terminalLinks = links.terminalLinks(executionId, outcome.result());
        return switch (outcome.status()) {
            case COMPLETED -> new TerminalEvent(
                    new TestExecutionCompletedEventAsyncDTO(
                            executionId.toString(), TestExecutionCompletedEventAsyncDTO.Status.COMPLETED,
                            "Test execution completed", outcome.result()
                                                               .id(),
                            terminalLinks.links()),
                    terminalLinks.resultUri());
            case FAILED -> new TerminalEvent(new TestExecutionFailedEventAsyncDTO(executionId.toString(),
                    TestExecutionFailedEventAsyncDTO.Status.FAILED, "Test execution failed", null,
                    terminalLinks.links()), terminalLinks.resultUri());
        };
    }

    public TestExecutionFailedEventAsyncDTO unpersistedFailureEvent(UUID executionId) {
        return new TestExecutionFailedEventAsyncDTO(executionId.toString(),
                TestExecutionFailedEventAsyncDTO.Status.FAILED, "Test execution failed", null,
                links.unpersistedFailureLinks(executionId));
    }

    /**
     * Event payload and the public URI of its persisted result.
     *
     * @param payload
     *            typed AsyncAPI terminal payload
     * @param resultUri
     *            public persisted result URI
     */
    public record TerminalEvent(Object payload, URI resultUri) {

        public TerminalEvent {
            Objects.requireNonNull(payload, "Terminal event payload is required");
            Objects.requireNonNull(resultUri, "Result URI is required");
        }

    }

}
