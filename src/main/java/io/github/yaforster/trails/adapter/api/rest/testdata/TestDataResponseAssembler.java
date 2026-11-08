package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TestDataEntry;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Maps test-data outcomes to contract DTOs and Trails-controlled navigation links.
 */
final class TestDataResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    TestDataSetDefinition definition(TestDataSetDefinitionDTO request) {
        return new TestDataSetDefinition(request.getLabel(), request.getValues()
                                                                    .stream()
                                                                    .map(entry -> new TestDataEntry(entry.getKey(),
                                                                            entry.getValue()))
                                                                    .toList());
    }

    TestDataSetDTO testData(TestDataSet dataSet, boolean includeRetired, boolean canAccessTestData) {
        Objects.requireNonNull(dataSet, "Test-data set is required");
        Map<String, LinkDTO> links = new LinkedHashMap<>();
        links.put("self", link(testDataUri(dataSet.id(), dataSet.retired()), HttpMethod.GET));
        if (canAccessTestData) {
            links.put("update", link(testDataItemUri(dataSet.id()), HttpMethod.PUT));
        }
        links.put("collection", link(collectionUri(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired), HttpMethod.GET));
        if (canAccessTestData) {
            if (dataSet.retired()) {
                links.put("restore", link(testDataOperationUri(dataSet.id()), HttpMethod.POST));
            } else {
                links.put("delete", link(testDataItemUri(dataSet.id()), HttpMethod.DELETE));
            }
        }
        return new TestDataSetDTO().id(dataSet.id())
                                   .label(dataSet.label())
                                   .creationDate(OffsetDateTime.ofInstant(dataSet.creationDate(), ZoneOffset.UTC))
                                   .retired(dataSet.retired())
                                   .values(dataSet.values()
                                                  .stream()
                                                  .map(this::entry)
                                                  .toList())
                                   .links(Map.copyOf(links));
    }

    PagedTestDataSetDTO testDataPage(PagedResult<TestDataSet> page, boolean includeRetired, boolean canAccessTestData) {
        Objects.requireNonNull(page, "Test-data page is required");
        PagedTestDataSetDTO response = new PagedTestDataSetDTO();
        response.setPage(page.page());
        response.setSize(page.size());
        response.setTotalElements(Math.toIntExact(page.totalItems()));
        response.setTotalPages(page.totalPages());
        response.setItems(page.items()
                              .stream()
                              .map(dataSet -> testData(dataSet, includeRetired, canAccessTestData))
                              .toList());
        response.setLinks(pagingLinks(page, includeRetired, canAccessTestData));
        return response;
    }

    URI location(TestDataSet dataSet) {
        Objects.requireNonNull(dataSet, "Test-data set is required");
        return URI.create("/api/test-data/" + dataSet.id());
    }

    DatabaseDeletionResultDTO deletion(DatabaseDeletionResult result) {
        return DeletionResponseMapper.toDto(result,
                Map.of("collection", link(collectionUri(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false), HttpMethod.GET)));
    }

    HttpStatus deletionStatus(DatabaseDeletionResult result) {
        return DeletionResponseMapper.status(result);
    }

    private TestDataEntryDTO entry(TestDataEntry entry) {
        return new TestDataEntryDTO().key(entry.key())
                                     .value(entry.value());
    }

    private Map<String, LinkDTO> pagingLinks(PagedResult<TestDataSet> page, boolean includeRetired,
            boolean canAccessTestData) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(
                PaginationLinks.forPage(page, pageNumber -> collectionUri(pageNumber, page.size(), includeRetired)));
        if (canAccessTestData) {
            links.put("create", link("/api/test-data", HttpMethod.PUT));
        }
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri, HttpMethod method) {
        return new LinkDTO().href(uri)
                            .method(method.name())
                            .templated(false);
    }

    private String collectionUri(int page, int size, boolean includeRetired) {
        return "/api/test-data?page=" + page + "&size=" + size + "&includeRetired=" + includeRetired;
    }

    private String testDataUri(Long testDataId, boolean includeRetired) {
        return "/api/test-data/" + testDataId + "?includeRetired=" + includeRetired;
    }

    private String testDataItemUri(Long testDataId) {
        return "/api/test-data/" + testDataId;
    }

    private String testDataOperationUri(Long testDataId) {
        return "/api/test-data/" + testDataId + "/" + "restore";
    }

}
