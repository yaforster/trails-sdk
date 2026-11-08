package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;

/**
 * Maps persisted execution paths to contract DTOs scoped to one verified test-set hierarchy.
 */
final class TestPathResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    TestPathResultDTO pathResult(Long applicationId, Long stageId, Long testRunId, PersistedTestPathResult result) {
        Objects.requireNonNull(result, "Persisted test path result is required");
        return new TestPathResultDTO().id(result.id())
                                      .links(links(applicationId, stageId, testRunId, result.testSetResultId(),
                                              result.id()));
    }

    PagedTestPathResultDTO pathResults(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            PagedResult<PersistedTestPathResult> page) {
        Objects.requireNonNull(page, "Test path result page is required");
        PagedTestPathResultDTO response = new PagedTestPathResultDTO().page(page.page())
                                                                      .size(page.size())
                                                                      .totalElements(Math.toIntExact(page.totalItems()))
                                                                      .totalPages(page.totalPages())
                                                                      .items(page.items()
                                                                                 .stream()
                                                                                 .map(result -> pathResult(
                                                                                         applicationId, stageId,
                                                                                         testRunId, result))
                                                                                 .toList());
        response.setLinks(pagingLinks(applicationId, stageId, testRunId, testSetResultId, page));
        return response;
    }

    private Map<String, LinkDTO> links(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId) {
        return Map.of("self", link(pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId)),
                "collection",
                link(pathsUri(applicationId, stageId, testRunId, testSetResultId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE)),
                "testSetResult", link(testSetUri(applicationId, stageId, testRunId, testSetResultId)), "actionResults",
                link(pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId) + "/actions/chain"),
                "downloadedFiles",
                link(pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId)
                        + "/artifacts?page=0&size=20"),
                "print", link(pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId) + "/print"));
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            PagedResult<PersistedTestPathResult> page) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(PaginationLinks.forPage(page,
                pageNumber -> pathsUri(applicationId, stageId, testRunId, testSetResultId, pageNumber, page.size())));
        links.put("testSetResult", link(testSetUri(applicationId, stageId, testRunId, testSetResultId)));
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

    private String testSetUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
        return testRunUri(applicationId, stageId, testRunId) + "/browsers/" + testSetResultId;
    }

    private String pathsUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, int page,
            int size) {
        return testSetUri(applicationId, stageId, testRunId, testSetResultId) + "/paths?page=" + page + "&size=" + size;
    }

    private String pathUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
        return testSetUri(applicationId, stageId, testRunId, testSetResultId) + "/paths/" + pathResultId;
    }

}
