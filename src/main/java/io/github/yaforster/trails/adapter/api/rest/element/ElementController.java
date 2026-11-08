package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.openapitools.api.ElementApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Exposes the contract-defined element resource through the validated persistence composition.
 */
@RestController
public class ElementController implements ElementApi {

    private final ElementDatabaseService elements;
    private final ScreenshotDatabaseService screenshots;
    private final StageDatabaseService stages;
    private final TrailsAuthorization authorization;
    private final ElementRequestValidator requests;
    private final ScreenshotFileRequestMapper uploads;
    private final ElementResponseAssembler responses;

    ElementController(ElementDatabaseService elements, ScreenshotDatabaseService screenshots,
            StageDatabaseService stages, TrailsAuthorization authorization, ElementRequestValidator requests,
            ScreenshotFileRequestMapper uploads, ElementResponseAssembler responses) {
        this.elements = Objects.requireNonNull(elements, "Element database service is required");
        this.screenshots = Objects.requireNonNull(screenshots, "Screenshot database service is required");
        this.stages = Objects.requireNonNull(stages, "Stage database service is required");
        this.authorization = Objects.requireNonNull(authorization, "Trails authorization is required");
        this.requests = Objects.requireNonNull(requests, "Element request validator is required");
        this.uploads = Objects.requireNonNull(uploads, "Element screenshot upload mapper is required");
        this.responses = Objects.requireNonNull(responses, "Element response assembler is required");
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<PersistedElementDTO> createNewElement(Long applicationId, Long stageId,
            ElementDefinitionDTO request) {
        requests.validateCreate(applicationId, stageId, request);
        PersistedElement element = elements.storeElement(
                new ElementDatabaseService.ElementCreation(applicationId, stageId, toDefinition(request)));
        return ResponseEntity.ok(responses.persistedElement(element));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> deleteElement(Long applicationId, Long stageId, Long elementId) {
        requests.validateElementReference(applicationId, stageId, elementId);
        return deletionResponse(applicationId, stageId,
                elements.deleteElement(new ElementDatabaseService.ElementReference(applicationId, stageId, elementId)));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> deleteElementScreenshot(Long applicationId, Long stageId,
            Long elementId) {
        requests.validateElementReference(applicationId, stageId, elementId);
        if (elements.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, false))
                    .isEmpty()) {
            return deletionResponse(applicationId, stageId, new DeletionNotFound(elementId));
        }
        return deletionResponse(applicationId, stageId, screenshots.deleteElementScreenshot(elementId));
    }

    @Override
    public ResponseEntity<ElementDTO> getElement(Long applicationId, Long stageId, Long elementId,
            Boolean includeRetired) {
        requests.validateElementReference(applicationId, stageId, elementId);
        return elements.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId,
                Boolean.TRUE.equals(includeRetired)))
                       .map(element -> responses.element(applicationId, stageId, element,
                               screenshots.findIdByElementId(element.id()), Boolean.TRUE.equals(includeRetired),
                               authorization.canManageResources()))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    public ResponseEntity<Resource> getElementScreenshot(Long applicationId, Long stageId, Long elementId) {
        requests.validateElementReference(applicationId, stageId, elementId);
        if (elements.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, false))
                    .isEmpty()) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return screenshots.loadScreenshotByElementId(elementId)
                          .map(screenshot -> ResponseEntity.ok()
                                                           .contentType(
                                                                   MediaType.parseMediaType(screenshot.contentType()))
                                                           .contentLength(screenshot.size())
                                                           .header(HttpHeaders.CONTENT_DISPOSITION,
                                                                   "inline; filename=\"" + screenshot.fileName() + "\"")
                                                           .<Resource>body(new ByteArrayResource(screenshot.content()
                                                                                                           .bytes())))
                          .orElseGet(() -> ResponseEntity.notFound()
                                                         .build());
    }

    @Override
    public ResponseEntity<PagedElementDTO> listElements(Long applicationId, Long stageId, Integer page, Integer size,
            Boolean includeRetired) {
        requests.validatePage(applicationId, stageId, page, size);
        PagedResult<PersistedElement> elementsPage = elements.getElements(new ElementDatabaseService.ElementPage(
                applicationId, stageId, page, size, Boolean.TRUE.equals(includeRetired)));
        List<Long> elementIds = elementsPage.items()
                                            .stream()
                                            .map(PersistedElement::id)
                                            .toList();
        Map<Long, Long> screenshotIds = screenshots.findAllIdsByElementIds(
                new ScreenshotDatabaseService.ElementScreenshotIds(elementIds));
        return ResponseEntity.ok(responses.elements(applicationId, stageId, elementsPage, screenshotIds,
                Boolean.TRUE.equals(includeRetired), authorization.canManageResources()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<ElementDTO> promoteElement(Long applicationId, Long sourceStageId, Long elementId,
            Long targetStageId) {
        requests.validatePromotion(applicationId, sourceStageId, elementId, targetStageId);
        if (!bothStagesExist(applicationId, sourceStageId, targetStageId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return elements.promoteElement(
                new ElementDatabaseService.ElementPromotion(applicationId, sourceStageId, elementId, targetStageId))
                       .map(element -> promotedElement(applicationId, targetStageId, element))
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> restoreElement(Long applicationId, Long stageId, Long elementId) {
        requests.validateElementReference(applicationId, stageId, elementId);
        return deletionResponse(applicationId, stageId, elements.restoreElement(
                new ElementDatabaseService.ElementReference(applicationId, stageId, elementId)));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<ElementDTO> updateElement(Long applicationId, Long stageId, Long elementId,
            ElementDefinitionDTO request) {
        requests.validateUpdate(applicationId, stageId, elementId, request);
        return elements.updateElement(
                new ElementDatabaseService.ElementUpdate(applicationId, stageId, elementId, toDefinition(request)))
                       .map(element -> responses.element(applicationId, stageId, element,
                               screenshots.findIdByElementId(element.id()), false, authorization.canManageResources()))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<ArtifactDTO> uploadElementScreenshot(Long applicationId, Long stageId, Long elementId,
            MultipartFile file) {
        requests.validateUpload(applicationId, stageId, elementId, file);
        TrailsScreenshotFile screenshot = uploads.from(file);
        if (elements.getElement(new ElementDatabaseService.ElementDetails(applicationId, stageId, elementId, false))
                    .isEmpty()) {
            return ResponseEntity.notFound()
                                 .build();
        }
        screenshots.setScreenshot(new ScreenshotDatabaseService.ElementScreenshot(elementId, screenshot));
        return screenshots.findIdByElementId(elementId)
                          .map(screenshotId -> ResponseEntity.ok(responses.screenshotArtifact(applicationId, stageId,
                                  elementId, screenshotId, screenshot)))
                          .orElseGet(() -> ResponseEntity.internalServerError()
                                                         .build());
    }

    private ElementDefinition toDefinition(ElementDefinitionDTO request) {
        return new ElementDefinition(ElementType.valueOf(request.getType()
                                                                .name()),
                request.getLabel(), request.getLocatorString(), LocatorType.valueOf(request.getLocatorType()
                                                                                           .name()));
    }

    private boolean bothStagesExist(Long applicationId, Long sourceStageId, Long targetStageId) {
        return stages.getStage(new StageDatabaseService.StageDetails(applicationId, sourceStageId, false))
                     .isPresent()
                && stages.getStage(new StageDatabaseService.StageDetails(applicationId, targetStageId, false))
                         .isPresent();
    }

    private ResponseEntity<ElementDTO> promotedElement(Long applicationId, Long targetStageId,
            PersistedElement element) {
        return ResponseEntity.created(responses.promotedElementLocation(applicationId, targetStageId, element.id()))
                             .body(responses.element(applicationId, targetStageId, element,
                                     screenshots.findIdByElementId(element.id()), false,
                                     authorization.canManageResources()));
    }

    private ResponseEntity<DatabaseDeletionResultDTO> deletionResponse(Long applicationId, Long stageId,
            DatabaseDeletionResult result) {
        return ResponseEntity.status(responses.deletionStatus(result))
                             .body(responses.deletion(applicationId, stageId, result));
    }

}
