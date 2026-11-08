package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedElementDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedElementDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Maps element outcomes to contract DTOs and Trails-controlled navigation links. */
final class ElementResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    PersistedElementDTO persistedElement(PersistedElement element) {
        Objects.requireNonNull(element, "Persisted element is required");
        return new PersistedElementDTO().id(element.id())
                                        .type(ElementTypeDTO.valueOf(element.type()
                                                                            .name()))
                                        .locator(element.locator())
                                        .locatorString(element.locator())
                                        .locatorType(LocatorTypeDTO.valueOf(element.locatorType()
                                                                                   .name()))
                                        .label(element.label())
                                        .retired(element.retired());
    }

    ElementDTO element(Long applicationId, Long stageId, PersistedElement element, Optional<Long> screenshotId,
            boolean includeRetired, boolean canManageResources) {
        Objects.requireNonNull(element, "Persisted element is required");
        Objects.requireNonNull(screenshotId, "Screenshot identifier is required");
        Map<String, LinkDTO> links = new LinkedHashMap<>();
        links.put("collection",
                link(collectionUri(applicationId, stageId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired),
                        HttpMethod.GET));
        screenshotId.ifPresent(id -> links.put("screenshot",
                link(screenshotUri(applicationId, stageId, element.id()), HttpMethod.GET)));
        links.put("self", link(elementUri(applicationId, stageId, element.id(), includeRetired), HttpMethod.GET));
        addMutationLinks(applicationId, stageId, element, screenshotId, canManageResources, links);
        return new ElementDTO().id(element.id())
                               .type(ElementTypeDTO.valueOf(element.type()
                                                                   .name()))
                               .label(element.label())
                               .locatorString(element.locator())
                               .locatorType(LocatorTypeDTO.valueOf(element.locatorType()
                                                                          .name()))
                               .retired(element.retired())
                               .links(Map.copyOf(links));
    }

    PagedElementDTO elements(Long applicationId, Long stageId, PagedResult<PersistedElement> page,
            Map<Long, Long> screenshotIds, boolean includeRetired, boolean canManageResources) {
        Objects.requireNonNull(page, "Element page is required");
        Objects.requireNonNull(screenshotIds, "Screenshot identifiers are required");
        PagedElementDTO response = new PagedElementDTO();
        response.setPage(page.page());
        response.setSize(page.size());
        response.setTotalElements(Math.toIntExact(page.totalItems()));
        response.setTotalPages(page.totalPages());
        response.setItems(page.items()
                              .stream()
                              .map(element -> element(applicationId, stageId, element,
                                      Optional.ofNullable(screenshotIds.get(element.id())), includeRetired,
                                      canManageResources))
                              .toList());
        response.setLinks(pagingLinks(applicationId, stageId, page, includeRetired, canManageResources));
        return response;
    }

    ArtifactDTO screenshotArtifact(Long applicationId, Long stageId, Long elementId, Long screenshotId,
            TrailsScreenshotFile file) {
        Objects.requireNonNull(screenshotId, "Screenshot identifier is required");
        Objects.requireNonNull(file, "Screenshot file is required");
        return new ArtifactDTO().id(screenshotId)
                                .type(ArtifactDTO.TypeEnum.SCREENSHOT)
                                .filename(file.filename())
                                .links(Map.of("self",
                                        link(screenshotUri(applicationId, stageId, elementId), HttpMethod.GET)));
    }

    DatabaseDeletionResultDTO deletion(Long applicationId, Long stageId, DatabaseDeletionResult result) {
        return DeletionResponseMapper.toDto(result, Map.of("collection",
                link(collectionUri(applicationId, stageId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false), HttpMethod.GET)));
    }

    HttpStatus deletionStatus(DatabaseDeletionResult result) {
        return DeletionResponseMapper.status(result);
    }

    URI promotedElementLocation(Long applicationId, Long stageId, Long elementId) {
        return URI.create(elementUri(applicationId, stageId, elementId, false));
    }

    private void addMutationLinks(Long applicationId, Long stageId, PersistedElement element,
            Optional<Long> screenshotId, boolean canManageResources, Map<String, LinkDTO> links) {
        if (!canManageResources) {
            return;
        }
        if (!element.retired()) {
            links.put("update", link(elementUri(applicationId, stageId, element.id(), false), HttpMethod.PUT));
            links.put("uploadScreenshot", link(screenshotUri(applicationId, stageId, element.id()), HttpMethod.PUT));
        }
        screenshotId.ifPresent(id -> links.put("deleteScreenshot",
                link(screenshotUri(applicationId, stageId, element.id()), HttpMethod.DELETE)));
        if (element.retired()) {
            links.put("restore",
                    link(elementOperationUri(applicationId, stageId, element.id(), "restore"), HttpMethod.POST));
            return;
        }
        links.put("delete", link(elementUri(applicationId, stageId, element.id(), false), HttpMethod.DELETE));
        links.put("promote",
                new LinkDTO().href(
                        elementOperationUri(applicationId, stageId, element.id(), "promote") + "{?targetStageId}")
                             .method(HttpMethod.POST.name())
                             .templated(true));
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, PagedResult<PersistedElement> page,
            boolean includeRetired, boolean canManageResources) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(PaginationLinks.forPage(page,
                pageNumber -> collectionUri(applicationId, stageId, pageNumber, page.size(), includeRetired)));
        if (canManageResources) {
            links.put("create", link(collectionUri(applicationId, stageId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false),
                    HttpMethod.PUT));
        }
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri, HttpMethod method) {
        return new LinkDTO().href(uri)
                            .method(method.name())
                            .templated(false);
    }

    private String collectionUri(Long applicationId, Long stageId, int page, int size, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/elements?page=" + page + "&size=" + size
                + "&includeRetired=" + includeRetired;
    }

    private String elementUri(Long applicationId, Long stageId, Long elementId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/elements/" + elementId
                + "?includeRetired=" + includeRetired;
    }

    private String screenshotUri(Long applicationId, Long stageId, Long elementId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/elements/" + elementId + "/screenshot";
    }

    private String elementOperationUri(Long applicationId, Long stageId, Long elementId, String operation) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/elements/" + elementId + "/" + operation;
    }

}
