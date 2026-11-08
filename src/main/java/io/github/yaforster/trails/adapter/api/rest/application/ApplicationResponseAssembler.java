package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedApplicationDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedApplicationDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Maps application outcomes to contract DTOs and their Trails-controlled links. */
final class ApplicationResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    ApplicationDTO application(PersistedApplication application, boolean hasStages, boolean includeRetired,
            boolean canManageResources) {
        Objects.requireNonNull(application, "Persisted application is required");
        return assembleApplication(application, hasStages, includeRetired, canManageResources);
    }

    PagedApplicationDTO applications(PagedResult<PersistedApplication> page, Set<Long> applicationIdsWithStages,
            boolean includeRetired, boolean canManageResources) {
        Objects.requireNonNull(page, "Application page is required");
        Objects.requireNonNull(applicationIdsWithStages, "Application stage inventory is required");
        PagedApplicationDTO response = new PagedApplicationDTO();
        response.setPage(page.page());
        response.setSize(page.size());
        response.setTotalElements(Math.toIntExact(page.totalItems()));
        response.setTotalPages(page.totalPages());
        response.setItems(page.items()
                              .stream()
                              .map(application -> assembleApplication(application,
                                      applicationIdsWithStages.contains(application.id()), includeRetired,
                                      canManageResources))
                              .toList());
        response.setLinks(pagingLinks(page, includeRetired, canManageResources));
        return response;
    }

    PersistedApplicationDTO persistedApplication(PersistedApplication application) {
        Objects.requireNonNull(application, "Persisted application is required");
        return new PersistedApplicationDTO().id(application.id())
                                            .label(application.label())
                                            .retired(application.retired());
    }

    DatabaseDeletionResultDTO deletion(DatabaseDeletionResult result) {
        return DeletionResponseMapper.toDto(result,
                Map.of("collection", link(collectionUri(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false), HttpMethod.GET)));
    }

    HttpStatus deletionStatus(DatabaseDeletionResult result) {
        return DeletionResponseMapper.status(result);
    }

    private ApplicationDTO assembleApplication(PersistedApplication application, boolean hasStages,
            boolean includeRetired, boolean canManageResources) {
        Map<String, LinkDTO> links = new LinkedHashMap<>();
        links.put("collection", link(collectionUri(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired), HttpMethod.GET));
        if (hasStages) {
            links.put("stages", link(stagesUri(application.id(), includeRetired), HttpMethod.GET));
        }
        links.put("self", link(applicationUri(application.id(), includeRetired), HttpMethod.GET));
        if (canManageResources) {
            if (application.retired()) {
                links.put("restore", link(applicationOperationUri(application.id(), "restore"), HttpMethod.POST));
            } else {
                links.put("delete", link(applicationOperationUri(application.id(), "delete"), HttpMethod.DELETE));
            }
        }
        return new ApplicationDTO().id(application.id())
                                   .label(application.label())
                                   .retired(application.retired())
                                   .links(links);
    }

    private Map<String, LinkDTO> pagingLinks(PagedResult<PersistedApplication> page, boolean includeRetired,
            boolean canManageResources) {
        Map<String, LinkDTO> links = PaginationLinks.forPage(page,
                number -> collectionUri(number, page.size(), includeRetired));
        if (canManageResources) {
            links.put("create", link(collectionUri(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired), HttpMethod.PUT));
        }
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri, HttpMethod method) {
        return new LinkDTO().href(uri)
                            .method(method.name())
                            .templated(false);
    }

    private String collectionUri(int page, int size, boolean includeRetired) {
        return "/api/applications?page=" + page + "&size=" + size + "&includeRetired=" + includeRetired;
    }

    private String applicationUri(Long applicationId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "?includeRetired=" + includeRetired;
    }

    private String applicationOperationUri(Long applicationId, String operation) {
        return "/api/applications/" + applicationId + "/" + operation;
    }

    private String stagesUri(Long applicationId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages?page=" + ApplicationResponseAssembler.DEFAULT_PAGE
                + "&size=" + ApplicationResponseAssembler.DEFAULT_PAGE_SIZE + "&includeRetired=" + includeRetired;
    }

}
