package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestCaseResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;

/**
 * Maps browser-execution outcomes to their contract representations and navigation links.
 */
final class TestSetResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    TestSetResultDTO browserResult(PersistedTestSetResult result) {
        Objects.requireNonNull(result, "Persisted test set result is required");
        TestSetResultDTO response = new TestSetResultDTO().id(result.id())
                                                          .totalRunTime(result.totalRunTime())
                                                          .browserToRunIn(BrowserDTO.valueOf(result.browserToRunIn()
                                                                                                   .name()))
                                                          .testPlanLabel(result.testPlanLabel())
                                                          .links(links(result));
        if (!PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP.equals(result.testCaseTimestamp())) {
            response.setTestCaseResult(new TestCaseResultDTO().timestamp(result.testCaseTimestamp())
                                                              .testedInBrowser(
                                                                      BrowserDTO.valueOf(result.testedInBrowser()
                                                                                               .name())));
        }
        return response;
    }

    PagedTestSetResultDTO browserResults(Long applicationId, Long stageId, Long testRunId,
            PagedResult<PersistedTestSetResult> page) {
        Objects.requireNonNull(page, "Test set result page is required");
        PagedTestSetResultDTO response = new PagedTestSetResultDTO().page(page.page())
                                                                    .size(page.size())
                                                                    .totalElements(Math.toIntExact(page.totalItems()))
                                                                    .totalPages(page.totalPages())
                                                                    .items(page.items()
                                                                               .stream()
                                                                               .map(this::browserResult)
                                                                               .toList());
        response.setLinks(pagingLinks(applicationId, stageId, testRunId, page));
        return response;
    }

    private Map<String, LinkDTO> links(PersistedTestSetResult result) {
        return Map.of("self",
                link(browserResultUri(result.applicationId(), result.stageId(), result.testRunId(), result.id())),
                "collection",
                link(browserResultsUri(result.applicationId(), result.stageId(), result.testRunId(), DEFAULT_PAGE,
                        DEFAULT_PAGE_SIZE)),
                "testRun", link(testRunUri(result.applicationId(), result.stageId(), result.testRunId())), "paths",
                link(pathsUri(result.applicationId(), result.stageId(), result.testRunId(), result.id())), "print",
                link(browserResultUri(result.applicationId(), result.stageId(), result.testRunId(), result.id())
                        + "/print"));
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, Long testRunId,
            PagedResult<PersistedTestSetResult> page) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(PaginationLinks.forPage(page,
                pageNumber -> browserResultsUri(applicationId, stageId, testRunId, pageNumber, page.size())));
        links.put("testRun", link(testRunUri(applicationId, stageId, testRunId)));
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri) {
        return new LinkDTO().href(uri)
                            .method(HttpMethod.GET.name())
                            .templated(false);
    }

    private String testRunUri(Long applicationId, Long stageId, Long testRunId) {
        return "/api/testruns/" + applicationId + "/" + stageId + "/" + testRunId;
    }

    private String browserResultsUri(Long applicationId, Long stageId, Long testRunId, int page, int size) {
        return testRunUri(applicationId, stageId, testRunId) + "/browsers?page=" + page + "&size=" + size;
    }

    private String browserResultUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
        return testRunUri(applicationId, stageId, testRunId) + "/browsers/" + testSetResultId;
    }

    private String pathsUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
        return browserResultUri(applicationId, stageId, testRunId, testSetResultId) + "/paths?page="
                + TestSetResponseAssembler.DEFAULT_PAGE + "&size=" + TestSetResponseAssembler.DEFAULT_PAGE_SIZE;
    }

}
