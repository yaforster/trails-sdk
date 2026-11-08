package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.app.services.*;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.*;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class TestRunExecutorTest {

    private static TestRunExecutor withExecutor(WebdriverConfigurationFactory browser,
            TestPlanDatabaseService testPlans, TestStepArrangementService testSteps, TimeSource time,
            TestRunResultDatabaseService results, Executor runExecutor) {
        ExecutorService testSetExecutor = new DirectExecutorService();
        return new TestRunExecutor(browser, testPlans, testSteps, time, results, runExecutor, testSetExecutor);
    }

    private static TestPlanDatabaseService testPlans(TestPlan plan) {
        TestPlanDatabaseService testPlans = mock();
        when(testPlans.getExecutableTestPlan(plan.getId())).thenReturn(Optional.of(plan));
        return testPlans;
    }

    private static TestPlan plan() {
        return new TestPlan(77L, 123L, 456L, "Plan label", List.of());
    }

    private static TestPlanRunDefinition definition() {
        return new TestPlanRunDefinition(77L, List.of(Browser.CHROME));
    }

    private static TestExecutionData executionData() {
        return new TestExecutionData(List.of(new WebDriverData(mock(DriverProvider.class), null, Browser.CHROME)),
                List.of(), 123L, 456L, 77L, "Plan label");
    }

    private static TimeSource time() {
        return () -> 1700000000000L;
    }

    private static TestSet testSet(Success actionResult) {
        TestCase testCase = mock();
        DriverProvider driver = mock();
        when(testCase.run(driver, Browser.CHROME)).thenReturn(new TestCaseResult(new Timestamp(1L), Browser.CHROME,
                List.of(new TestPathResult(List.of(actionResult), List.of()))));
        return new TestSet(testCase, driver, Browser.CHROME, "Plan label");
    }

    private static TestSetResult testSetResult(Result... actionResults) {
        return new TestSetResult(
                new TestCaseResult(new Timestamp(1L), Browser.CHROME,
                        List.of(new TestPathResult(List.of(actionResults), List.of()))),
                0, Browser.CHROME, "Plan label");
    }

    private static Success success() {
        return Success.builder()
                      .actionID(1L)
                      .label("action")
                      .resultMessage("ok")
                      .build();
    }

    private static ValidationFailure failure() {
        return ValidationFailure.builder()
                                .actionID(1L)
                                .label("action")
                                .resultMessage("failed")
                                .build();
    }

    private static PersistedTestRunResult persisted() {
        return new PersistedTestRunResult(99L, 123L, 456L, 77L, new Timestamp(1700000000000L), ResultIndicator.FAILURE,
                "Plan label");
    }

    private static TestService.TestRunLifecycle lifecycle(AtomicReference<TestRunTerminalOutcome> terminal) {
        return new TestService.TestRunLifecycle(() -> {
        }, terminal::set, ignored -> {
        });
    }

    @Test
    void shouldPersistCompletedResultAndPublishIt_whenBrowserExecutionCompletes() {
        TestPlanDatabaseService testPlans = testPlans(plan());
        WebdriverConfigurationFactory browser = mock();
        TestStepArrangementService testSteps = mock();
        TestRunResultDatabaseService results = mock();
        TestSet testSet = testSet(success());
        when(browser.getTestExecutionData(definition())).thenReturn(executionData());
        when(testSteps.setUpTestsPerBrowser(executionData())).thenReturn(List.of(testSet));
        PersistedTestRunResult persisted = persisted();
        when(results.storeTestRunResult(org.mockito.ArgumentMatchers.any())).thenReturn(persisted);
        AtomicReference<TestRunTerminalOutcome> terminal = new AtomicReference<>();

        withExecutor(browser, testPlans, testSteps, time(), results, Runnable::run).runTest(
                new TestService.TestRunExecution(definition(), lifecycle(terminal)));

        ArgumentCaptor<TestRunResult> resultCaptor = ArgumentCaptor.forClass(TestRunResult.class);
        verify(results).storeTestRunResult(resultCaptor.capture());
        assertThat(terminal.get()).isEqualTo(new TestRunTerminalOutcome(persisted, TestRunTerminalStatus.COMPLETED));
    }

    @Test
    void shouldPersistSafeFailedResultAndPublishIt_whenBrowserSetupFails() {
        TestPlanDatabaseService testPlans = testPlans(plan());
        WebdriverConfigurationFactory browser = mock();
        TestStepArrangementService testSteps = mock();
        TestRunResultDatabaseService results = mock();
        when(browser.getTestExecutionData(definition())).thenThrow(new IllegalStateException("browser unavailable"));
        PersistedTestRunResult persisted = persisted();
        when(results.storeTestRunResult(org.mockito.ArgumentMatchers.any())).thenReturn(persisted);
        AtomicReference<TestRunTerminalOutcome> terminal = new AtomicReference<>();

        withExecutor(browser, testPlans, testSteps, time(), results, Runnable::run).runTest(
                new TestService.TestRunExecution(definition(), lifecycle(terminal)));

        ArgumentCaptor<TestRunResult> resultCaptor = ArgumentCaptor.forClass(TestRunResult.class);
        verify(results).storeTestRunResult(resultCaptor.capture());
        assertThat(resultCaptor.getValue()).extracting(TestRunResult::indicator, TestRunResult::testResults)
                                           .containsExactly(ResultIndicator.FAILURE, List.of());
        assertThat(terminal.get()).isEqualTo(new TestRunTerminalOutcome(persisted, TestRunTerminalStatus.FAILED));
    }

    @Test
    void shouldPersistSafeFailedResult_whenRunAdmissionIsRejected() {
        TestPlanDatabaseService testPlans = testPlans(plan());
        TestRunResultDatabaseService results = mock();
        PersistedTestRunResult persisted = persisted();
        when(results.storeTestRunResult(org.mockito.ArgumentMatchers.any())).thenReturn(persisted);
        AtomicReference<TestRunTerminalOutcome> terminal = new AtomicReference<>();
        Executor rejectingExecutor = ignored -> {
            throw new RejectedExecutionException("queue is full");
        };

        withExecutor(mock(), testPlans, mock(), time(), results, rejectingExecutor).runTest(
                new TestService.TestRunExecution(definition(), lifecycle(terminal)));

        ArgumentCaptor<TestRunResult> resultCaptor = ArgumentCaptor.forClass(TestRunResult.class);
        verify(results).storeTestRunResult(resultCaptor.capture());
        assertThat(resultCaptor.getValue()).extracting(TestRunResult::indicator, TestRunResult::testResults)
                                           .containsExactly(ResultIndicator.FAILURE, List.of());
        assertThat(terminal.get()).isEqualTo(new TestRunTerminalOutcome(persisted, TestRunTerminalStatus.FAILED));
    }

    @Test
    void shouldReportUnpersistedFailure_whenTestPlanDoesNotExistBeforeAdmission() {
        TestPlanDatabaseService testPlans = mock();
        TestRunResultDatabaseService results = mock();
        when(testPlans.getExecutableTestPlan(definition().testPlanID())).thenReturn(Optional.empty());
        AtomicReference<Exception> failure = new AtomicReference<>();
        TestService.TestRunLifecycle lifecycle = new TestService.TestRunLifecycle(() -> {
        }, ignored -> {
        }, failure::set);

        assertThatThrownBy(() -> withExecutor(mock(), testPlans, mock(), time(), results, Runnable::run).runTest(
                new TestService.TestRunExecution(definition(), lifecycle))).isInstanceOf(EntityMissingException.class);

        assertThat(failure.get()).isInstanceOf(EntityMissingException.class);
        verifyNoInteractions(results);
    }

    @Test
    void shouldReportUnpersistedFailure_whenSafeFailureResultCannotBeStored() {
        TestPlanDatabaseService testPlans = testPlans(plan());
        WebdriverConfigurationFactory browser = mock();
        TestRunResultDatabaseService results = mock();
        IllegalStateException browserFailure = new IllegalStateException("browser unavailable");
        IllegalStateException persistenceFailure = new IllegalStateException("database unavailable");
        when(browser.getTestExecutionData(definition())).thenThrow(browserFailure);
        when(results.storeTestRunResult(org.mockito.ArgumentMatchers.any())).thenThrow(persistenceFailure);
        AtomicReference<TestRunTerminalOutcome> terminal = new AtomicReference<>();
        AtomicReference<Exception> unpersistedFailure = new AtomicReference<>();
        TestService.TestRunLifecycle lifecycle = new TestService.TestRunLifecycle(() -> {
        }, terminal::set, unpersistedFailure::set);

        withExecutor(browser, testPlans, mock(), time(), results, Runnable::run).runTest(
                new TestService.TestRunExecution(definition(), lifecycle));

        assertThat(unpersistedFailure.get()).isSameAs(persistenceFailure);
        assertThat(unpersistedFailure.get()
                                     .getSuppressed()).containsExactly(browserFailure);
        assertThat(terminal.get()).isNull();
    }

    @Test
    void shouldClassifyMixedActionResultsAsPartialSuccess() {
        TestSetResult result = testSetResult(success(), failure());

        assertThat(TestRunExecutor.determineResultIndicator(List.of(result))).isEqualTo(
                ResultIndicator.PARTIAL_SUCCESS);
    }

    @Test
    void shouldClassifyAbsentActionResultsAsFailure() {
        TestSetResult result = testSetResult();

        assertThat(TestRunExecutor.determineResultIndicator(List.of(result))).isEqualTo(ResultIndicator.FAILURE);
    }

    private static final class DirectExecutorService extends AbstractExecutorService {

        private boolean shutdown;

        @Override
        public void shutdown() {
            shutdown = true;
        }

        @Override
        public List<Runnable> shutdownNow() {
            shutdown = true;
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return shutdown;
        }

        @Override
        public boolean isTerminated() {
            return shutdown;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return shutdown;
        }

        @Override
        public void execute(Runnable command) {
            if (shutdown) {
                throw new RejectedExecutionException();
            }
            command.run();
        }

    }

}
