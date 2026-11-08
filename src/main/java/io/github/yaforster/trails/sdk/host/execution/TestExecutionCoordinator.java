package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.sdk.notification.NotifierDispatcher;
import io.github.yaforster.trails.sdk.notification.TerminalRunNotification;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Coordinates lifecycle events and notification admission around SDK-owned test execution.
 */
public final class TestExecutionCoordinator implements TestExecutionFacadeService {

    private final TestService testService;
    private final TestExecutionEventPublisher eventPublisher;
    private final NotifierDispatcher notifiers;

    public TestExecutionCoordinator(TestService testService, TestExecutionEventPublisher eventPublisher,
            NotifierDispatcher notifiers) {
        this.testService = Objects.requireNonNull(testService, "Test service is required");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "Test execution event publisher is required");
        this.notifiers = Objects.requireNonNull(notifiers, "Notifier dispatcher is required");
    }

    @Override
    public void executeTest(TestExecution execution) {
        testService.runTest(new TestService.TestRunExecution(execution.runDefinition(),
                new TestService.TestRunLifecycle(() -> eventPublisher.publishStarted(execution.executionId()),
                        outcome -> publishTerminalOutcome(execution, outcome),
                        exception -> eventPublisher.publishUnpersistedFailure(execution.executionId()))));
    }

    private void publishTerminalOutcome(TestExecution execution, TestRunTerminalOutcome outcome) {
        Optional<URI> resultUri = eventPublisher.publishTerminal(execution.executionId(), outcome);
        notifiers.dispatch(new TerminalRunNotification(execution.executionId(), outcome.result()
                                                                                       .id(),
                outcome.status(), outcome.result()
                                         .status(),
                resultUri, execution.emailRecipients()));
    }

}
