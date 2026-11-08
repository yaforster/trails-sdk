package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Maps stage outcomes to contract DTOs and their Trails-controlled links.
 */
final class StageResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    StageDTO stage(Long applicationId, PersistedStage stage, boolean hasElements, boolean includeRetired,
            boolean canManageResources) {
        Objects.requireNonNull(stage, "Persisted stage is required");
        return assembleStage(applicationId, stage, hasElements, includeRetired, canManageResources);
    }

    PagedStageDTO stages(Long applicationId, PagedResult<PersistedStage> page, Set<Long> stageIdsWithElements,
            boolean includeRetired, boolean canManageResources) {
        Objects.requireNonNull(page, "Stage page is required");
        Objects.requireNonNull(stageIdsWithElements, "Stage element inventory is required");
        PagedStageDTO response = new PagedStageDTO();
        response.setPage(page.page());
        response.setSize(page.size());
        response.setTotalElements(Math.toIntExact(page.totalItems()));
        response.setTotalPages(page.totalPages());
        response.setItems(page.items()
                              .stream()
                              .map(stage -> assembleStage(applicationId, stage,
                                      stageIdsWithElements.contains(stage.id()), includeRetired, canManageResources))
                              .toList());
        response.setLinks(pagingLinks(applicationId, page, includeRetired, canManageResources));
        return response;
    }

    PersistedStageDTO persistedStage(Long applicationId, PersistedStage stage) {
        Objects.requireNonNull(stage, "Persisted stage is required");
        return new PersistedStageDTO().id(stage.id())
                                      .label(stage.label())
                                      .url(URI.create(stage.url()))
                                      .retired(stage.retired())
                                      .links(Map.of("testPlans",
                                              link(testPlansUri(applicationId, stage.id(), stage.retired()),
                                                      HttpMethod.GET),
                                              "testRuns",
                                              link(testRunsUri(applicationId, stage.id()), HttpMethod.GET)));
    }

    DatabaseDeletionResultDTO deletion(Long applicationId, DatabaseDeletionResult result) {
        return DeletionResponseMapper.toDto(result, Map.of("collection",
                link(collectionUri(applicationId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false), HttpMethod.GET)));
    }

    HttpStatus deletionStatus(DatabaseDeletionResult result) {
        return DeletionResponseMapper.status(result);
    }

    private StageDTO assembleStage(Long applicationId, PersistedStage stage, boolean hasElements,
            boolean includeRetired, boolean canManageResources) {
        Map<String, LinkDTO> links = new LinkedHashMap<>();
        links.put("collection",
                link(collectionUri(applicationId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired), HttpMethod.GET));
        if (hasElements) {
            links.put("elements", link(elementsUri(applicationId, stage.id(), includeRetired), HttpMethod.GET));
        }
        links.put("testPlans", link(testPlansUri(applicationId, stage.id(), includeRetired), HttpMethod.GET));
        links.put("testRuns", link(testRunsUri(applicationId, stage.id()), HttpMethod.GET));
        links.put("self", link(stageUri(applicationId, stage.id(), includeRetired), HttpMethod.GET));
        if (canManageResources) {
            if (stage.retired()) {
                links.put("restore", link(stageOperationUri(applicationId, stage.id(), "restore"), HttpMethod.POST));
            } else {
                links.put("delete", link(stageOperationUri(applicationId, stage.id(), "delete"), HttpMethod.DELETE));
            }
        }
        return new StageDTO().id(stage.id())
                             .label(stage.label())
                             .url(stage.url())
                             .retired(stage.retired())
                             .links(links);
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, PagedResult<PersistedStage> page,
            boolean includeRetired, boolean canManageResources) {
        Map<String, LinkDTO> links = PaginationLinks.forPage(page,
                number -> collectionUri(applicationId, number, page.size(), includeRetired));
        if (canManageResources) {
            links.put("create", link(collectionUri(applicationId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired),
                    HttpMethod.PUT));
        }
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri, HttpMethod method) {
        return new LinkDTO().href(uri)
                            .method(method.name())
                            .templated(false);
    }

    private String collectionUri(Long applicationId, int page, int size, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages?page=" + page + "&size=" + size + "&includeRetired="
                + includeRetired;
    }

    private String stageUri(Long applicationId, Long stageId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "?includeRetired=" + includeRetired;
    }

    private String stageOperationUri(Long applicationId, Long stageId, String operation) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/" + operation;
    }

    private String elementsUri(Long applicationId, Long stageId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/elements?page="
                + StageResponseAssembler.DEFAULT_PAGE + "&size=" + StageResponseAssembler.DEFAULT_PAGE_SIZE
                + "&includeRetired=" + includeRetired;
    }

    private String testPlansUri(Long applicationId, Long stageId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans?page="
                + StageResponseAssembler.DEFAULT_PAGE + "&size=" + StageResponseAssembler.DEFAULT_PAGE_SIZE
                + "&includeRetired=" + includeRetired;
    }

    private String testRunsUri(Long applicationId, Long stageId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testruns?page="
                + StageResponseAssembler.DEFAULT_PAGE + "&size=" + StageResponseAssembler.DEFAULT_PAGE_SIZE;
    }

}
