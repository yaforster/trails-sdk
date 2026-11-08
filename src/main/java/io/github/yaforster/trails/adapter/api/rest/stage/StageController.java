package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.openapitools.api.StageApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Exposes the contract-defined stage resource through the validated persistence composition.
 */
@RestController
public class StageController implements StageApi {

    private final StageDatabaseService stages;
    private final ElementDatabaseService elements;
    private final TrailsAuthorization authorization;
    private final StageRequestValidator requests;
    private final StageResponseAssembler responses;

    StageController(StageDatabaseService stages, ElementDatabaseService elements, TrailsAuthorization authorization,
            StageRequestValidator requests, StageResponseAssembler responses) {
        this.stages = Objects.requireNonNull(stages, "Stage database service is required");
        this.elements = Objects.requireNonNull(elements, "Element database service is required");
        this.authorization = Objects.requireNonNull(authorization, "Trails authorization is required");
        this.requests = Objects.requireNonNull(requests, "Stage request validator is required");
        this.responses = Objects.requireNonNull(responses, "Stage response assembler is required");
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<PersistedStageDTO> createNewStage(Long applicationId, StageDefinitionDTO request) {
        requests.validateCreate(applicationId, request);
        PersistedStage stage = stages.storeStage(new StageDatabaseService.StageCreation(applicationId,
                new StageDefinition(request.getLabel(), request.getUrl()
                                                               .toString(),
                        List.of())));
        return ResponseEntity.ok(responses.persistedStage(applicationId, stage));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> deleteStage(Long applicationId, Long stageId) {
        requests.validateStageReference(applicationId, stageId);
        return deletionResponse(applicationId,
                stages.deleteStage(new StageDatabaseService.StageReference(applicationId, stageId)));
    }

    @Override
    public ResponseEntity<StageDTO> getStage(Long applicationId, Long stageId, Boolean includeRetired) {
        return stages.getStage(
                new StageDatabaseService.StageDetails(applicationId, stageId, Boolean.TRUE.equals(includeRetired)))
                     .map(stage -> responses.stage(applicationId, stage,
                             elements.hasElements(new ElementDatabaseService.StageReference(applicationId, stage.id())),
                             Boolean.TRUE.equals(includeRetired), authorization.canManageResources()))
                     .map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound()
                                                    .build());
    }

    @Override
    public ResponseEntity<PagedStageDTO> listStages(Long applicationId, Integer page, Integer size,
            Boolean includeRetired) {
        requests.validatePage(applicationId, page, size);
        boolean retirementIncluded = Boolean.TRUE.equals(includeRetired);
        StageDatabaseService.StagePage query = new StageDatabaseService.StagePage(applicationId, page, size,
                retirementIncluded);
        PagedResult<PersistedStage> stagesPage = stages.getStages(query);
        Set<Long> stageIdsWithElements = elements.findStageIdsWithElements(
                new ElementDatabaseService.CandidateStages(applicationId, stagesPage.items()
                                                                                    .stream()
                                                                                    .map(PersistedStage::id)
                                                                                    .toList()));
        return ResponseEntity.ok(responses.stages(applicationId, stagesPage, stageIdsWithElements, retirementIncluded,
                authorization.canManageResources()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> restoreStage(Long applicationId, Long stageId) {
        requests.validateStageReference(applicationId, stageId);
        return deletionResponse(applicationId,
                stages.restoreStage(new StageDatabaseService.StageReference(applicationId, stageId)));
    }

    private ResponseEntity<DatabaseDeletionResultDTO> deletionResponse(Long applicationId,
            DatabaseDeletionResult result) {
        return ResponseEntity.status(responses.deletionStatus(result))
                             .body(responses.deletion(applicationId, result));
    }

}
