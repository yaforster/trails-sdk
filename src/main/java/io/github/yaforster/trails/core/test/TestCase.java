package io.github.yaforster.trails.core.test;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Aggregates the independent execution paths of one test case. */
public class TestCase {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCase.class);
    private final TestStep startStep;
    private final TestPathRunner pathRunner;
    private final Clock clock;

    public TestCase(URL applicationURL, TestStep startStep) {
        this(applicationURL, startStep, Clock.systemDefaultZone());
    }

    public TestCase(URL applicationURL, TestStep startStep, Clock clock) {
        this.startStep = Objects.requireNonNull(startStep, "Starting test step is required");
        this.clock = Objects.requireNonNull(clock, "Clock is required");
        this.pathRunner = new TestPathRunner(Objects.requireNonNull(applicationURL, "Application URL is required"),
                this.clock);
    }

    public TestCaseResult run(DriverProvider webdriverProvider, Browser browserToRunIn) {
        List<TestPathResult> pathResults = runActionsAndCollectResults(webdriverProvider);
        Timestamp executedAt = Timestamp.from(clock.instant());
        return new TestCaseResult(executedAt, browserToRunIn, pathResults);
    }

    List<TestPathResult> runActionsAndCollectResults(DriverProvider webdriverProvider) {
        return executeActionsAndCollectResults(webdriverProvider);
    }

    List<TestPathResult> executeActionsAndCollectResults(DriverProvider webdriverProvider) {
        List<List<TestStep>> executionPaths = startStep.getTestPathsFromThisStep();
        return executionPaths.stream()
                             .map(steps -> runTestsUntilFailure(webdriverProvider, steps))
                             .toList();
    }

    TestPathResult runTestsUntilFailure(DriverProvider webdriverProvider, List<TestStep> steps) {
        return pathRunner.run(webdriverProvider, steps);
    }
}
