package io.github.yaforster.trails.sdk.host.print;

import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.PrintFacadeService;
import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.GeneratedFile;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.print.TestPathPrintContext;
import io.github.yaforster.trails.core.print.TestRunPrintContext;
import io.github.yaforster.trails.core.print.TestSetPrintContext;
import io.github.yaforster.trails.core.test.Browser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * Builds complete, hierarchy-checked print documents from the selected persistence capability.
 */
public final class PrintFacade implements PrintFacadeService {

    private static final int QUERY_PAGE_SIZE = 100;
    private final TestSetResultQueryService testSets;
    private final TestPathResultQueryService paths;
    private final ActionResultQueryService actionResults;
    private final TestRunResultDatabaseService testRuns;
    private final PrintService printer;
    private final HierarchyValidationService hierarchies;

    public PrintFacade(TestSetResultQueryService testSets, TestPathResultQueryService paths,
            ActionResultQueryService actionResults, TestRunResultDatabaseService testRuns, PrintService printer,
            HierarchyValidationService hierarchies) {
        this.testSets = Objects.requireNonNull(testSets, "Test set result query service is required");
        this.paths = Objects.requireNonNull(paths, "Test path result query service is required");
        this.actionResults = Objects.requireNonNull(actionResults, "Action result query service is required");
        this.testRuns = Objects.requireNonNull(testRuns, "Test run result database service is required");
        this.printer = Objects.requireNonNull(printer, "Print service is required");
        this.hierarchies = Objects.requireNonNull(hierarchies, "Hierarchy validation service is required");
    }

    @Override
    public Optional<GeneratedFile> printPath(TestPathPrintData request) {
        if (!pathBelongsToHierarchy(request)) {
            return Optional.empty();
        }
        return pathContext(request.pathResultId(), request.testSetResultId()).map(context -> new GeneratedFile(
                "test-path-" + request.pathResultId() + ".pdf",
                outputStream -> printer.renderTestPathPdf(new PrintService.TestPathPdf(context, outputStream))));
    }

    @Override
    public Optional<GeneratedFile> printTestSet(TestSetPrintData request) {
        if (!testSetBelongsToRun(request)) {
            return Optional.empty();
        }
        return testSets.getTestSetResult(request.testSetResultId())
                       .flatMap(this::testSetContext)
                       .map(context -> new GeneratedFile("test-set-" + request.testSetResultId() + ".pdf",
                               outputStream -> printer.renderTestSetPdf(
                                       new PrintService.TestSetPdf(context, outputStream))));
    }

    @Override
    public Optional<GeneratedFile> printTestRun(TestRunPrintData request) {
        return testRunContext(request.applicationId(), request.stageId(), request.testRunId()).map(
                context -> new GeneratedFile("test-run-" + request.testRunId() + ".pdf",
                        outputStream -> printer.renderTestRunPdf(new PrintService.TestRunPdf(context, outputStream))));
    }

    private Optional<TestPathPrintContext> pathContext(Long pathResultId, Long testSetResultId) {
        Optional<PersistedTestPathResult> pathResult = paths.getPathResult(pathResultId);
        if (pathResult.isEmpty() || !Objects.equals(pathResult.get()
                                                              .testSetResultId(),
                testSetResultId)) {
            return Optional.empty();
        }
        Optional<List<PersistedActionResult>> actionChain = actionResults.listActionResultChain(pathResultId);
        Optional<Browser> browser = testSets.getTestSetResult(testSetResultId)
                                            .flatMap(this::executedBrowser);
        if (actionChain.isEmpty() || browser.isEmpty()) {
            return Optional.empty();
        }
        PersistedTestPathResult selectedPath = pathResult.get();
        return Optional.of(new TestPathPrintContext(selectedPath.id(), selectedPath.testSetResultId(),
                selectedPath.pathLabel(), browser.get(), actionChain.get()));
    }

    private Optional<TestSetPrintContext> testSetContext(PersistedTestSetResult testSet) {
        return testSetContexts(List.of(testSet)).flatMap(contexts -> contexts.stream()
                                                                             .findFirst());
    }

    private Optional<TestRunPrintContext> testRunContext(Long applicationId, Long stageId, Long testRunId) {
        Optional<PersistedTestRunResult> testRun = testRuns.getTestRunResult(
                new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId));
        Optional<List<PersistedTestSetResult>> testSetsForRun = pages(
                page -> testSets.listBrowserResults(new TestSetResultQueryService.TestRunBrowserResults(applicationId,
                        stageId, testRunId, page, QUERY_PAGE_SIZE)));
        if (testRun.isEmpty() || testSetsForRun.isEmpty()) {
            return Optional.empty();
        }
        PersistedTestRunResult result = testRun.get();
        return testSetContexts(testSetsForRun.get()).map(
                contexts -> new TestRunPrintContext(result.id(), result.applicationId(), result.stageId(),
                        result.testPlanId(), result.timestamp(), result.status(), result.label(), contexts));
    }

    private Optional<List<TestSetPrintContext>> testSetContexts(List<PersistedTestSetResult> setResults) {
        if (setResults.isEmpty()) {
            return Optional.of(List.of());
        }
        Map<Long, Browser> browsersBySetId = setResults.stream()
                                                       .map(result -> executedBrowser(result).map(
                                                               browser -> Map.entry(result.id(), browser)))
                                                       .flatMap(Optional::stream)
                                                       .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                                                               (first, _) -> first));
        if (browsersBySetId.size() != setResults.size()) {
            return Optional.empty();
        }
        return pathResults(browsersBySetId.keySet()).flatMap(
                pathResults -> assembleTestSets(setResults, browsersBySetId, pathResults));
    }

    private Optional<List<TestSetPrintContext>> assembleTestSets(List<PersistedTestSetResult> setResults,
            Map<Long, Browser> browsersBySetId, List<PersistedTestPathResult> pathResults) {
        Set<Long> pathIds = pathResults.stream()
                                       .map(PersistedTestPathResult::id)
                                       .collect(Collectors.toSet());
        if (pathIds.size() != pathResults.size() || pathResults.stream()
                                                               .anyMatch(path -> !browsersBySetId.containsKey(
                                                                       path.testSetResultId()))) {
            return Optional.empty();
        }
        if (pathResults.isEmpty()) {
            return Optional.of(assembleTestSetContexts(setResults, browsersBySetId, Map.of(), Map.of()));
        }
        List<Long> requestedPathIds = pathResults.stream()
                                                 .map(PersistedTestPathResult::id)
                                                 .toList();
        ActionResultQueryService.ActionResultChains query = new ActionResultQueryService.ActionResultChains(
                requestedPathIds);
        Map<Long, List<PersistedActionResult>> actionChains = actionResults.listActionResultChains(query);
        if (actionChains == null || !actionChains.keySet()
                                                 .equals(pathIds)
                || actionChains.values()
                               .stream()
                               .anyMatch(Objects::isNull)) {
            return Optional.empty();
        }
        Map<Long, List<PersistedTestPathResult>> pathsBySet = pathResults.stream()
                                                                         .collect(Collectors.groupingBy(
                                                                                 PersistedTestPathResult::testSetResultId));
        return Optional.of(assembleTestSetContexts(setResults, browsersBySetId, pathsBySet, actionChains));
    }

    private List<TestSetPrintContext> assembleTestSetContexts(List<PersistedTestSetResult> setResults,
            Map<Long, Browser> browsersBySetId, Map<Long, List<PersistedTestPathResult>> pathsBySet,
            Map<Long, List<PersistedActionResult>> actionChains) {
        return setResults.stream()
                         .map(testSet -> assembleTestSetContext(testSet, browsersBySetId, pathsBySet, actionChains))
                         .toList();
    }

    private TestSetPrintContext assembleTestSetContext(PersistedTestSetResult testSet,
            Map<Long, Browser> browsersBySetId, Map<Long, List<PersistedTestPathResult>> pathsBySet,
            Map<Long, List<PersistedActionResult>> actionChains) {
        Browser browser = browsersBySetId.get(testSet.id());
        List<PersistedTestPathResult> pathsForSet = pathsBySet.getOrDefault(testSet.id(), List.of());
        List<TestPathPrintContext> pathContexts = pathsForSet.stream()
                                                             .map(path -> new TestPathPrintContext(path.id(),
                                                                     path.testSetResultId(), path.pathLabel(), browser,
                                                                     actionChains.get(path.id())))
                                                             .toList();
        return new TestSetPrintContext(testSet.id(), testSet.testRunId(), testSet.applicationId(), testSet.stageId(),
                browser, pathContexts);
    }

    private Optional<List<PersistedTestPathResult>> pathResults(Collection<Long> testSetResultIds) {
        return pages(page -> Optional.ofNullable(paths.listPathResults(
                new TestPathResultQueryService.TestSetPaths(testSetResultIds, page, QUERY_PAGE_SIZE))));
    }

    private <T> Optional<List<T>> pages(IntFunction<Optional<PagedResult<T>>> loader) {
        List<T> results = new ArrayList<>();
        int page = 0;
        Optional<PagedResult<T>> resultPage;
        do {
            resultPage = loader.apply(page++);
            if (resultPage.isEmpty()) {
                return Optional.empty();
            }
            results.addAll(resultPage.get()
                                     .items());
        } while (resultPage.get()
                           .hasNext());
        return Optional.of(results);
    }

    private boolean pathBelongsToHierarchy(TestPathPrintData request) {
        return hierarchies.pathBelongsToHierarchy(
                new HierarchyValidationService.TestPathHierarchy(request.applicationId(), request.stageId(),
                        request.testRunId(), request.testSetResultId(), request.pathResultId()));
    }

    private boolean testSetBelongsToRun(TestSetPrintData request) {
        return hierarchies.testSetBelongsToRun(new HierarchyValidationService.TestSetHierarchy(request.applicationId(),
                request.stageId(), request.testRunId(), request.testSetResultId()));
    }

    private Optional<Browser> executedBrowser(PersistedTestSetResult testSet) {
        return Optional.ofNullable(testSet.testedInBrowser())
                       .or(() -> Optional.ofNullable(testSet.browserToRunIn()));
    }

}
