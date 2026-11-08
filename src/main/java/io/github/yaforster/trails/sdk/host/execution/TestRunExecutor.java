package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.app.services.TestStepArrangementService;
import io.github.yaforster.trails.app.services.TimeSource;
import io.github.yaforster.trails.app.services.WebdriverConfigurationFactory;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import io.github.yaforster.trails.core.test.TestRunResult;
import io.github.yaforster.trails.core.test.TestSet;
import io.github.yaforster.trails.core.test.TestSetResult;
import io.github.yaforster.trails.core.test.result.Success;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Executes complete browser test runs with separate, bounded admission and test-set executors.
 */
public final class TestRunExecutor implements TestService, AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestRunExecutor.class);
    private final WebdriverConfigurationFactory browser;
    private final TestPlanDatabaseService testPlans;
    private final TestStepArrangementService testSteps;
    private final TimeSource time;
    private final TestRunResultDatabaseService results;
    private final Executor runExecutor;
    private final ExecutorService testSetExecutor;
    private final Runnable closeExecutors;

    public TestRunExecutor(WebdriverConfigurationFactory browser, TestPlanDatabaseService testPlans,
            TestStepArrangementService testSteps, TimeSource time, TestRunResultDatabaseService results,
            TestExecutionLimits limits) {
        this(browser, testPlans, testSteps, time, results, TestExecutionExecutors.create(limits));
    }

    private TestRunExecutor(WebdriverConfigurationFactory browser, TestPlanDatabaseService testPlans,
            TestStepArrangementService testSteps, TimeSource time, TestRunResultDatabaseService results,
            TestExecutionExecutors executors) {
        this(browser, testPlans, testSteps, time, results, executors.runs(), executors.testSets(), executors::close);
    }

    TestRunExecutor(WebdriverConfigurationFactory browser, TestPlanDatabaseService testPlans,
            TestStepArrangementService testSteps, TimeSource time, TestRunResultDatabaseService results,
            Executor runExecutor, ExecutorService testSetExecutor) {
        this(browser, testPlans, testSteps, time, results, runExecutor, testSetExecutor, () -> {
        });
    }

    private TestRunExecutor(WebdriverConfigurationFactory browser, TestPlanDatabaseService testPlans,
            TestStepArrangementService testSteps, TimeSource time, TestRunResultDatabaseService results,
            Executor runExecutor, ExecutorService testSetExecutor, Runnable closeExecutors) {
        this.browser = Objects.requireNonNull(browser, "Browser configuration factory is required");
        this.testPlans = Objects.requireNonNull(testPlans, "Test plan persistence is required");
        this.testSteps = Objects.requireNonNull(testSteps, "Test step arrangement is required");
        this.time = Objects.requireNonNull(time, "Time source is required");
        this.results = Objects.requireNonNull(results, "Test result persistence is required");
        this.runExecutor = Objects.requireNonNull(runExecutor, "Test run executor is required");
        this.testSetExecutor = Objects.requireNonNull(testSetExecutor, "Test set executor is required");
        this.closeExecutors = Objects.requireNonNull(closeExecutors, "Executor shutdown is required");
    }

    @Override
    public void runTest(TestRunExecution execution) {
        ExecutionTarget target = resolveExecutionTarget(execution);
        admitExecution(execution, target);
    }

    @Override
    public void close() {
        closeExecutors.run();
    }

    static ResultIndicator determineResultIndicator(List<TestSetResult> testSetResults) {
        ResultStats resultStats = summarizeResultStats(testSetResults);
        if (resultStats.total() == 0 || resultStats.successes() == 0) {
            return ResultIndicator.FAILURE;
        }
        if (resultStats.total() == resultStats.successes()) {
            return ResultIndicator.SUCCESS;
        }
        return ResultIndicator.PARTIAL_SUCCESS;
    }

    private ExecutionTarget resolveExecutionTarget(TestRunExecution execution) {
        try {
            Long testPlanId = execution.runDefinition()
                                       .testPlanID();
            TestPlan testPlan = testPlans.getExecutableTestPlan(testPlanId)
                                         .orElseThrow(() -> new EntityMissingException(
                                                 "Test plan with id " + testPlanId + " could not be found."));
            return ExecutionTarget.from(testPlan);
        } catch (RuntimeException exception) {
            execution.lifecycle()
                     .onUnpersistedFailure()
                     .accept(exception);
            throw exception;
        }
    }

    private void admitExecution(TestRunExecution execution, ExecutionTarget target) {
        AdmittedTestRun admitted = new AdmittedTestRun(() -> executeAdmittedRun(execution, target),
                () -> persistAndPublishFailure(execution, target,
                        new RejectedExecutionException("Test execution cancelled before start during shutdown")));
        try {
            runExecutor.execute(admitted);
        } catch (RejectedExecutionException exception) {
            persistAndPublishFailure(execution, target, exception);
        }
    }

    private void executeAdmittedRun(TestRunExecution execution, ExecutionTarget target) {
        TestRunTerminalOutcome terminalOutcome;
        try {
            execution.lifecycle()
                     .onStart()
                     .run();
            terminalOutcome = completedOutcome(target, execution.runDefinition());
        } catch (RuntimeException exception) {
            persistAndPublishFailure(execution, target, exception);
            return;
        }
        publishTerminalOutcome(execution, terminalOutcome);
    }

    private void persistAndPublishFailure(TestRunExecution execution, ExecutionTarget target,
            RuntimeException failure) {
        LOGGER.warn("Test execution failed applicationId={} stageId={} testPlanId={} error={} cause={}",
                target.applicationId(), target.stageId(), target.testPlanId(), failure.getClass()
                                                                                      .getSimpleName(),
                exceptionCauseType(failure));
        LOGGER.debug("Test execution failure stack trace", failure);
        TestRunTerminalOutcome terminalOutcome;
        try {
            terminalOutcome = failedOutcome(target);
        } catch (RuntimeException persistenceFailure) {
            persistenceFailure.addSuppressed(failure);
            execution.lifecycle()
                     .onUnpersistedFailure()
                     .accept(persistenceFailure);
            return;
        }
        publishTerminalOutcome(execution, terminalOutcome);
    }

    private TestRunTerminalOutcome completedOutcome(ExecutionTarget target, TestPlanRunDefinition runDefinition) {
        TestExecutionData executionData = browser.getTestExecutionData(runDefinition);
        target.requireMatching(executionData);
        List<TestSetResult> testSetResults = executeTestSets(executionData);
        TestRunResult result = new TestRunResult(testSetResults, timestamp(), determineResultIndicator(testSetResults),
                target.applicationId(), target.stageId(), target.testPlanId(), target.label());
        return new TestRunTerminalOutcome(results.storeTestRunResult(result), TestRunTerminalStatus.COMPLETED);
    }

    private TestRunTerminalOutcome failedOutcome(ExecutionTarget target) {
        TestRunResult result = new TestRunResult(List.of(), timestamp(), ResultIndicator.FAILURE,
                target.applicationId(), target.stageId(), target.testPlanId(), target.label());
        return new TestRunTerminalOutcome(results.storeTestRunResult(result), TestRunTerminalStatus.FAILED);
    }

    private List<TestSetResult> executeTestSets(TestExecutionData executionData) {
        List<Future<TestSetResult>> submittedTestSets = new ArrayList<>();
        try {
            for (TestSet testSet : testSteps.setUpTestsPerBrowser(executionData)) {
                submittedTestSets.add(testSetExecutor.submit(testSet::runTests));
            }
            return submittedTestSets.stream()
                                    .map(this::awaitTestSetResult)
                                    .toList();
        } catch (RuntimeException exception) {
            submittedTestSets.forEach(testSet -> testSet.cancel(true));
            throw exception;
        }
    }

    private void publishTerminalOutcome(TestRunExecution execution, TestRunTerminalOutcome outcome) {
        execution.lifecycle()
                 .onTerminal()
                 .accept(outcome);
    }

    private Timestamp timestamp() {
        return new Timestamp(time.currentTimeMillis());
    }

    private String exceptionCauseType(RuntimeException failure) {
        return java.util.Optional.ofNullable(failure.getCause())
                                 .map(cause -> cause.getClass()
                                                    .getSimpleName())
                                 .orElse("none");
    }

    private TestSetResult awaitTestSetResult(Future<TestSetResult> submittedTestSet) {
        try {
            return submittedTestSet.get();
        } catch (InterruptedException exception) {
            Thread.currentThread()
                  .interrupt();
            throw new IllegalStateException("Interrupted while waiting for browser test execution", exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Browser test execution failed", exception.getCause());
        }
    }

    private static ResultStats summarizeResultStats(List<TestSetResult> testSetResults) {
        return testSetResults.stream()
                             .flatMap(testSetResult -> testSetResult.testCaseResult()
                                                                    .results()
                                                                    .stream())
                             .flatMap(testPathResult -> testPathResult.results()
                                                                      .stream())
                             .collect(Collectors.teeing(Collectors.counting(),
                                     Collectors.filtering(Success.class::isInstance, Collectors.counting()),
                                     ResultStats::new));
    }

}
