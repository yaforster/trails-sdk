package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.app.services.*;
import io.github.yaforster.trails.core.data.*;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.test.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class TestRunShutdownTest {
    @Test
    void shouldPublishBothFailures_whenShutdownInterruptsRunningAndCancelsQueuedRun() throws Exception {
        CountDownLatch running = new CountDownLatch(1);
        WebdriverConfigurationFactory browser = mock();
        when(browser.getTestExecutionData(any())).thenAnswer(_ -> {
            running.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException exception) {
                throw new IllegalStateException("Interrupted browser setup", exception);
            }
            throw new AssertionError("Browser setup should be interrupted");
        });
        TestPlanDatabaseService plans = mock();
        when(plans.getExecutableTestPlan(7L)).thenReturn(Optional.of(new TestPlan(7L, 1L, 2L, "Plan", List.of())));
        TestRunResultDatabaseService results = mock();
        when(results.storeTestRunResult(any())).thenReturn(
                new PersistedTestRunResult(8L, 1L, 2L, 7L, new Timestamp(0), ResultIndicator.FAILURE, "Plan"));
        List<TestRunTerminalOutcome> terminals = new CopyOnWriteArrayList<>();
        List<Exception> failures = new CopyOnWriteArrayList<>();
        TestService.TestRunLifecycle lifecycle = new TestService.TestRunLifecycle(() -> {
        }, terminals::add, failures::add);
        TestService.TestRunExecution request = new TestService.TestRunExecution(
                new TestPlanRunDefinition(7L, List.of(Browser.CHROME)), lifecycle);
        TestExecutionLimits limits = new TestExecutionLimits(1, 1, 1, Duration.ofMillis(100));
        try (TestRunExecutor executor = new TestRunExecutor(browser, plans, mock(), () -> 0L, results, limits)) {
            executor.runTest(request);
            if (!running.await(5, TimeUnit.SECONDS))
                throw new AssertionError("Run did not start");
            executor.runTest(request);
        }
        assertThat(terminals).extracting(TestRunTerminalOutcome::status)
                             .containsExactly(TestRunTerminalStatus.FAILED, TestRunTerminalStatus.FAILED);
        verify(results, times(2)).storeTestRunResult(any());
        verify(browser).getTestExecutionData(any());
    }
}
