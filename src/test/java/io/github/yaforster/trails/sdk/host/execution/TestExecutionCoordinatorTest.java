package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import io.github.yaforster.trails.core.notification.EmailRecipients;
import io.github.yaforster.trails.sdk.notification.NotifierDispatcher;
import io.github.yaforster.trails.sdk.notification.TerminalRunNotification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import java.net.URI;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TestExecutionCoordinatorTest {

    private static TestService.TestRunLifecycle capturedLifecycle(TestService testService) {
        ArgumentCaptor<TestService.TestRunExecution> execution = ArgumentCaptor.forClass(
                TestService.TestRunExecution.class);
        verify(testService).runTest(execution.capture());
        return execution.getValue()
                        .lifecycle();
    }

    private static TestRunTerminalOutcome outcome(TestRunTerminalStatus status) {
        PersistedTestRunResult result = new PersistedTestRunResult(99L, 1L, 2L, 3L, new Timestamp(1L),
                ResultIndicator.SUCCESS, "Plan label");
        return new TestRunTerminalOutcome(result, status);
    }

    @Test
    void shouldPublishTerminalEventBeforeDispatchingNotification_whenExecutionCompletes() {
        TestService testService = mock();
        TestExecutionEventPublisher events = mock();
        NotifierDispatcher notifiers = mock();
        TestExecutionCoordinator coordinator = new TestExecutionCoordinator(testService, events, notifiers);
        UUID executionId = UUID.randomUUID();
        TestExecutionFacadeService.TestExecution execution = new TestExecutionFacadeService.TestExecution(executionId,
                new TestPlanRunDefinition(1L, List.of(Browser.CHROME)),
                Optional.of(new EmailRecipients(List.of("alice@example.test"))));

        coordinator.executeTest(execution);
        TestService.TestRunLifecycle lifecycle = capturedLifecycle(testService);
        TestRunTerminalOutcome outcome = outcome(TestRunTerminalStatus.COMPLETED);
        URI resultUri = URI.create("/api/testruns/1/2/99");
        when(events.publishTerminal(executionId, outcome)).thenReturn(Optional.of(resultUri));
        lifecycle.onStart()
                 .run();
        lifecycle.onTerminal()
                 .accept(outcome);

        InOrder order = inOrder(events, notifiers);
        order.verify(events)
             .publishStarted(executionId);
        order.verify(events)
             .publishTerminal(executionId, outcome);
        ArgumentCaptor<TerminalRunNotification> notification = ArgumentCaptor.forClass(TerminalRunNotification.class);
        order.verify(notifiers)
             .dispatch(notification.capture());
        assertThat(notification.getValue()).isEqualTo(
                new TerminalRunNotification(executionId, 99L, TestRunTerminalStatus.COMPLETED, ResultIndicator.SUCCESS,
                        Optional.of(resultUri), execution.emailRecipients()));
    }

    @Test
    void shouldPublishUnpersistedFailureWithoutDispatchingNotification_whenResultCannotBePersisted() {
        TestService testService = mock();
        TestExecutionEventPublisher events = mock();
        NotifierDispatcher notifiers = mock();
        TestExecutionCoordinator coordinator = new TestExecutionCoordinator(testService, events, notifiers);
        UUID executionId = UUID.randomUUID();

        coordinator.executeTest(new TestExecutionFacadeService.TestExecution(executionId,
                new TestPlanRunDefinition(1L, List.of(Browser.CHROME))));
        capturedLifecycle(testService).onUnpersistedFailure()
                                      .accept(new IllegalStateException("database unavailable"));

        verify(events).publishUnpersistedFailure(executionId);
        verifyNoInteractions(notifiers);
    }

    @Test
    void shouldDispatchFailedTerminalStatus_whenExecutionFailsAfterPersistence() {
        TestService testService = mock();
        TestExecutionEventPublisher events = mock();
        NotifierDispatcher notifiers = mock();
        TestExecutionCoordinator coordinator = new TestExecutionCoordinator(testService, events, notifiers);
        UUID executionId = UUID.randomUUID();

        coordinator.executeTest(new TestExecutionFacadeService.TestExecution(executionId,
                new TestPlanRunDefinition(1L, List.of(Browser.CHROME))));
        capturedLifecycle(testService).onTerminal()
                                      .accept(outcome(TestRunTerminalStatus.FAILED));

        ArgumentCaptor<TerminalRunNotification> notification = ArgumentCaptor.forClass(TerminalRunNotification.class);
        verify(notifiers).dispatch(notification.capture());
        assertThat(notification.getValue()
                               .status()).isEqualTo(TestRunTerminalStatus.FAILED);
    }

    @ParameterizedTest
    @EnumSource(ResultIndicator.class)
    void shouldPreservePersistedTestOutcome_whenExecutionCompletes(ResultIndicator indicator) {
        TestService testService = mock();
        TestExecutionEventPublisher events = mock();
        NotifierDispatcher notifiers = mock();
        TestExecutionCoordinator coordinator = new TestExecutionCoordinator(testService, events, notifiers);
        coordinator.executeTest(new TestExecutionFacadeService.TestExecution(UUID.randomUUID(),
                new TestPlanRunDefinition(1L, List.of(Browser.CHROME))));
        PersistedTestRunResult result = new PersistedTestRunResult(99L, 1L, 2L, 3L, new Timestamp(1L), indicator,
                "Plan");

        capturedLifecycle(testService).onTerminal()
                                      .accept(new TestRunTerminalOutcome(result, TestRunTerminalStatus.COMPLETED));

        ArgumentCaptor<TerminalRunNotification> notification = ArgumentCaptor.forClass(TerminalRunNotification.class);
        verify(notifiers).dispatch(notification.capture());
        assertThat(notification.getValue()
                               .resultIndicator()).isEqualTo(indicator);
    }

}
