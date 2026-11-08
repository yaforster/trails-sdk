package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunHistoryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ResultIndicatorDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunHistoryPointDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunStatisticsDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;

/** Maps persisted test-run outcomes to contract DTOs and SDK-owned navigation links. */
final class TestRunResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int HISTORY_PAGE_SIZE = 50;

    PersistedTestRunResultDTO testRun(PersistedTestRunResult result) {
        Objects.requireNonNull(result, "Persisted test run is required");
        return new PersistedTestRunResultDTO().id(result.id())
                                              .timestamp(toOffsetDateTime(result.timestamp()))
                                              .indicator(ResultIndicatorDTO.fromValue(result.status()
                                                                                            .name()))
                                              .applicationId(result.applicationId())
                                              .stageId(result.stageId())
                                              .label(result.label())
                                              .links(testRunLinks(result));
    }

    PagedTestRunDTO testRuns(Long applicationId, Long stageId, PagedResult<PersistedTestRunResult> page) {
        Objects.requireNonNull(page, "Test run page is required");
        PagedTestRunDTO response = new PagedTestRunDTO().page(page.page())
                                                        .size(page.size())
                                                        .totalElements(Math.toIntExact(page.totalItems()))
                                                        .totalPages(page.totalPages())
                                                        .items(page.items()
                                                                   .stream()
                                                                   .map(this::testRun)
                                                                   .toList());
        response.setLinks(pagingLinks(applicationId, stageId, page));
        return response;
    }

    TestRunStatisticsDTO statistics(PersistedTestRunStatistics statistics) {
        Objects.requireNonNull(statistics, "Test run statistics are required");
        return new TestRunStatisticsDTO().applicationId(statistics.applicationId())
                                         .stageId(statistics.stageId())
                                         .testPlanId(statistics.testPlanId())
                                         .totalRuns(statistics.totalRuns())
                                         .successfulRuns(statistics.successfulRuns())
                                         .partialSuccessRuns(statistics.partialSuccessRuns())
                                         .failedRuns(statistics.failedRuns())
                                         .links(statisticsLinks(statistics));
    }

    PagedTestRunHistoryDTO history(PersistedTestRunHistory history) {
        Objects.requireNonNull(history, "Test run history is required");
        PagedResult<PersistedTestRunResult> page = history.items();
        PagedTestRunHistoryDTO response = new PagedTestRunHistoryDTO().page(page.page())
                                                                      .size(page.size())
                                                                      .totalElements(Math.toIntExact(page.totalItems()))
                                                                      .totalPages(page.totalPages())
                                                                      .applicationId(history.applicationId())
                                                                      .stageId(history.stageId())
                                                                      .testPlanId(history.testPlanId())
                                                                      .totalRuns(history.totalRuns())
                                                                      .successfulRuns(history.successfulRuns())
                                                                      .partialSuccessRuns(history.partialSuccessRuns())
                                                                      .failedRuns(history.failedRuns())
                                                                      .items(page.items()
                                                                                 .stream()
                                                                                 .map(this::historyPoint)
                                                                                 .toList());
        response.setLinks(historyLinks(history));
        return response;
    }

    private TestRunHistoryPointDTO historyPoint(PersistedTestRunResult result) {
        return new TestRunHistoryPointDTO().id(result.id())
                                           .timestamp(toOffsetDateTime(result.timestamp()))
                                           .indicator(ResultIndicatorDTO.fromValue(result.status()
                                                                                         .name()))
                                           .label(result.label());
    }

    private Map<String, LinkDTO> testRunLinks(PersistedTestRunResult result) {
        return Map.of("self", link(testRunUri(result.applicationId(), result.stageId(), result.id())), "collection",
                link(testRunsUri(result.applicationId(), result.stageId(), DEFAULT_PAGE, DEFAULT_PAGE_SIZE)),
                "testSets", link(browserResultsUri(result.applicationId(), result.stageId(), result.id())), "print",
                link(printUri(result.applicationId(), result.stageId(), result.id())));
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId,
            PagedResult<PersistedTestRunResult> page) {
        return Map.copyOf(PaginationLinks.forPage(page,
                pageNumber -> testRunsUri(applicationId, stageId, pageNumber, page.size())));
    }

    private Map<String, LinkDTO> statisticsLinks(PersistedTestRunStatistics statistics) {
        return Map.of("self",
                link(statisticsUri(statistics.applicationId(), statistics.stageId(), statistics.testPlanId())),
                "history",
                link(historyUri(statistics.applicationId(), statistics.stageId(), statistics.testPlanId(), DEFAULT_PAGE,
                        HISTORY_PAGE_SIZE)),
                "testPlan",
                link(testPlanUri(statistics.applicationId(), statistics.stageId(), statistics.testPlanId())),
                "collection", link(testPlansUri(statistics.applicationId(), statistics.stageId())));
    }

    private Map<String, LinkDTO> historyLinks(PersistedTestRunHistory history) {
        PagedResult<PersistedTestRunResult> page = history.items();
        Map<String, LinkDTO> links = new LinkedHashMap<>(
                PaginationLinks.forPage(page, pageNumber -> historyUri(history.applicationId(), history.stageId(),
                        history.testPlanId(), pageNumber, page.size())));
        links.put("statistics", link(statisticsUri(history.applicationId(), history.stageId(), history.testPlanId())));
        links.put("testPlan", link(testPlanUri(history.applicationId(), history.stageId(), history.testPlanId())));
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri) {
        return new LinkDTO().href(uri)
                            .method(HttpMethod.GET.name())
                            .templated(false);
    }

    private OffsetDateTime toOffsetDateTime(Timestamp timestamp) {
        return java.util.Optional.ofNullable(timestamp)
                                 .map(value -> value.toInstant()
                                                    .atOffset(ZoneOffset.UTC))
                                 .orElse(null);
    }

    private String testRunsUri(Long applicationId, Long stageId, int page, int size) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testruns?page=" + page + "&size=" + size;
    }

    private String testRunUri(Long applicationId, Long stageId, Long testRunId) {
        return "/api/testruns/" + applicationId + "/" + stageId + "/" + testRunId;
    }

    private String browserResultsUri(Long applicationId, Long stageId, Long testRunId) {
        return testRunUri(applicationId, stageId, testRunId) + "/browsers?page=" + TestRunResponseAssembler.DEFAULT_PAGE
                + "&size=" + TestRunResponseAssembler.DEFAULT_PAGE_SIZE;
    }

    private String printUri(Long applicationId, Long stageId, Long testRunId) {
        return testRunUri(applicationId, stageId, testRunId) + "/print";
    }

    private String statisticsUri(Long applicationId, Long stageId, Long testPlanId) {
        return testPlanResourceUri(applicationId, stageId, testPlanId) + "/statistics";
    }

    private String historyUri(Long applicationId, Long stageId, Long testPlanId, int page, int size) {
        return testPlanResourceUri(applicationId, stageId, testPlanId) + "/metrics/history?page=" + page + "&size="
                + size;
    }

    private String testPlanUri(Long applicationId, Long stageId, Long testPlanId) {
        return testPlanResourceUri(applicationId, stageId, testPlanId) + "?includeRetired=false";
    }

    private String testPlanResourceUri(Long applicationId, Long stageId, Long testPlanId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId;
    }

    private String testPlansUri(Long applicationId, Long stageId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans?page="
                + TestRunResponseAssembler.DEFAULT_PAGE + "&size=" + TestRunResponseAssembler.DEFAULT_PAGE_SIZE
                + "&includeRetired=false";
    }

}
