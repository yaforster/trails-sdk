package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedStage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface StageDatabaseService {

    PersistedStage storeStage(StageCreation stageCreation);

    DatabaseDeletionResult deleteStage(StageReference stageReference);

    DatabaseDeletionResult restoreStage(StageReference stageReference);

    PagedResult<PersistedStage> getStages(StagePage stagePage);

    Optional<PersistedStage> getStage(StageDetails stageDetails);

    boolean hasStages(ApplicationReference applicationReference);

    Set<Long> findApplicationIdsWithStages(ApplicationsWithStages applications);

    boolean existsByLabel(StageLabel stageLabel);

    boolean existsByUrl(StageUrl stageUrl);

    record StageCreation(Long applicationId, StageDefinition stageDefinition) {
    }

    record StageReference(Long applicationId, Long stageId) {
    }

    record StagePage(Long applicationId, int page, int size, boolean includeRetired) {
    }

    record StageDetails(Long applicationId, Long stageId, boolean includeRetired) {
    }

    record ApplicationReference(Long applicationId) {
    }

    record ApplicationsWithStages(Collection<Long> applicationIds) {

        public ApplicationsWithStages {
            applicationIds = List.copyOf(applicationIds);
        }

    }

    record StageLabel(Long applicationId, String label) {
    }

    record StageUrl(Long applicationId, String url) {
    }

}
