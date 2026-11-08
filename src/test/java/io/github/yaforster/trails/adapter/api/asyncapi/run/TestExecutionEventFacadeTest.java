package io.github.yaforster.trails.adapter.api.asyncapi.run;

import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionCompletedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.asyncapi.model.TestExecutionFailedEventAsyncDTO;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionLinkFactory;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import java.sql.Timestamp;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestExecutionEventFacadeTest {

    private final TestExecutionEventFacade facade = new TestExecutionEventFacade(new TestExecutionLinkFactory());

    @Test
    void shouldPublishCompletedEventWithPersistedResultLink() {
        UUID executionId = UUID.randomUUID();

        TestExecutionEventFacade.TerminalEvent event = facade.terminalEvent(executionId,
                outcome(TestRunTerminalStatus.COMPLETED));

        assertThat(event.payload()).isInstanceOfSatisfying(TestExecutionCompletedEventAsyncDTO.class, completed -> {
            assertThat(completed.status()).isEqualTo(TestExecutionCompletedEventAsyncDTO.Status.COMPLETED);
            assertThat(completed.testRunId()).isEqualTo(99L);
            assertThat(completed.links()
                                .get("result")
                                .href()).isEqualTo("/api/testruns/1/2/99");
        });
        assertThat(event.resultUri()).hasToString("/api/testruns/1/2/99");
    }

    @Test
    void shouldPublishSafeFailedEventWithPersistedResultLink() {
        UUID executionId = UUID.randomUUID();

        TestExecutionEventFacade.TerminalEvent event = facade.terminalEvent(executionId,
                outcome(TestRunTerminalStatus.FAILED));

        assertThat(event.payload()).isInstanceOfSatisfying(TestExecutionFailedEventAsyncDTO.class, failed -> {
            assertThat(failed.status()).isEqualTo(TestExecutionFailedEventAsyncDTO.Status.FAILED);
            assertThat(failed.error()).isNull();
            assertThat(failed.links()
                             .get("result")
                             .href()).isEqualTo("/api/testruns/1/2/99");
        });
    }

    private TestRunTerminalOutcome outcome(TestRunTerminalStatus status) {
        return new TestRunTerminalOutcome(
                new PersistedTestRunResult(99L, 1L, 2L, 3L, new Timestamp(1L), ResultIndicator.FAILURE, "Plan"),
                status);
    }

}
