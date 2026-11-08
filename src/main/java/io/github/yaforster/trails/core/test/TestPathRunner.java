package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.result.Failure;
import io.github.yaforster.trails.core.test.result.SkippedResult;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import java.net.URL;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Owns one browser session from creation through result collection and cleanup. */
final class TestPathRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestPathRunner.class);
    private static final String PREVIOUS_FAILURE = "Skipped this action due to previous error.";
    private static final String BROWSER_FAILURE = "Skipped this action because the browser could not open or execute the test path.";
    private static final String BROWSER_TECHNICAL_FAILURE = "Could not open the browser test path.";
    private static final String ACTION_TECHNICAL_FAILURE = "Could not execute this action.";
    private static final String DOWNLOAD_FAILURE = "Could not collect downloaded files after executing this path.";
    private final URL applicationURL;
    private final Clock clock;

    TestPathRunner(URL applicationURL, Clock clock) {
        this.applicationURL = applicationURL;
        this.clock = clock;
    }

    TestPathResult run(DriverProvider webdriverProvider, List<TestStep> steps) {
        Optional<BrowserExecution> browserExecution;
        try {
            browserExecution = webdriverProvider.createDriver();
        } catch (Exception exception) {
            logFailure("Could not create browser session", exception);
            return failedPath(steps, exception);
        }
        if (browserExecution.isEmpty()) {
            return failedPath(steps, new IllegalStateException("Browser session was not available"));
        }
        BrowserExecution execution = browserExecution.get();
        BrowserSession browser = execution.browser();
        try {
            TestExecutionContext context = new TestExecutionContext(execution, clock);
            browser.open(applicationURL.toString());
            List<Result> results = runActions(steps, context);
            return collectPathResult(steps, results, context);
        } catch (Exception exception) {
            logFailure("Browser navigation failed", exception);
            return failedPath(steps, exception);
        } finally {
            cleanUp(browser);
        }
    }

    private List<Result> runActions(List<TestStep> steps, TestExecutionContext context) {
        List<Result> results = new ArrayList<>(steps.size());
        boolean continueTesting = true;
        for (TestStep step : steps) {
            if (continueTesting) {
                Result result = executeStep(step, context);
                results.add(result);
                continueTesting = !(result instanceof Failure);
            } else {
                results.add(skipped(step, PREVIOUS_FAILURE));
            }
        }
        return results;
    }

    private Result executeStep(TestStep step, TestExecutionContext context) {
        try {
            return step.action()
                       .execute(context);
        } catch (Exception exception) {
            logFailure("Action execution failed", exception);
            return technicalFailure(step, exception, ACTION_TECHNICAL_FAILURE);
        }
    }

    private TestPathResult collectPathResult(List<TestStep> steps, List<Result> results, TestExecutionContext context) {
        try {
            List<FileData> downloadedFiles = context.downloadedFiles();
            return new TestPathResult(results, downloadedFiles);
        } catch (Exception exception) {
            logFailure("Could not collect browser downloads", exception);
            List<Result> completedResults = new ArrayList<>(results);
            if (!steps.isEmpty()) {
                // An additional result preserves every executed action while marking path-level collection failure.
                completedResults.add(technicalFailure(steps.getLast(), exception, DOWNLOAD_FAILURE));
            }
            return new TestPathResult(completedResults, List.of());
        }
    }

    private TestPathResult failedPath(List<TestStep> steps, Exception exception) {
        if (steps.isEmpty()) {
            return new TestPathResult(List.of(), List.of());
        }
        List<Result> results = new ArrayList<>();
        results.add(technicalFailure(steps.getFirst(), exception));
        results.addAll(skip(steps.subList(1, steps.size())));
        return new TestPathResult(results, List.of());
    }

    private TechnicalFailure technicalFailure(TestStep step, Exception exception) {
        return technicalFailure(step, exception, BROWSER_TECHNICAL_FAILURE);
    }

    private TechnicalFailure technicalFailure(TestStep step, Exception exception, String message) {
        Action action = step.action();
        return TechnicalFailure.builder()
                               .actionID(action.getActionID())
                               .label(action.getLabel())
                               .resultMessage(message)
                               .exceptionMessageFromAction(exception.getClass()
                                                                    .getSimpleName())
                               .build();
    }

    private void logFailure(String message, Exception exception) {
        LOGGER.warn("{} error={}", message, exception.getClass()
                                                     .getSimpleName());
        LOGGER.debug("Browser path failure stack trace", exception);
    }

    private List<Result> skip(List<TestStep> steps) {
        return steps.stream()
                    .map(step -> (Result) skipped(step, TestPathRunner.BROWSER_FAILURE))
                    .toList();
    }

    private SkippedResult skipped(TestStep step, String message) {
        return SkippedResult.builder()
                            .actionID(step.action()
                                          .getActionID())
                            .label(step.action()
                                       .getLabel())
                            .resultMessage(message)
                            .build();
    }

    private void cleanUp(BrowserSession browser) {
        try {
            browser.clearDownloadedFiles();
        } catch (Exception exception) {
            LOGGER.warn("Could not clear browser downloads error={}", exception.getClass()
                                                                               .getSimpleName());
            LOGGER.debug("Browser download cleanup failure stack trace", exception);
        }
        try {
            browser.close();
        } catch (Exception exception) {
            LOGGER.warn("Could not close browser error={}", exception.getClass()
                                                                     .getSimpleName());
            LOGGER.debug("Browser close failure stack trace", exception);
        }
    }
}
