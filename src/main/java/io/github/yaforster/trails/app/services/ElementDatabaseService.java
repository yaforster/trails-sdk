package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.Locator;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ElementDatabaseService {

    PersistedElement storeElement(ElementCreation elementCreation);

    Optional<PersistedElement> updateElement(ElementUpdate elementUpdate);

    Optional<PersistedElement> getElement(ElementDetails elementDetails);

    DatabaseDeletionResult deleteElement(ElementReference elementReference);

    DatabaseDeletionResult restoreElement(ElementReference elementReference);

    Optional<PersistedElement> promoteElement(ElementPromotion elementPromotion);

    PagedResult<PersistedElement> getElements(ElementPage elementPage);

    boolean hasElements(StageReference stageReference);

    Set<Long> findStageIdsWithElements(CandidateStages candidateStages);

    boolean existsByLabel(ElementLabel elementLabel);

    boolean existsByLocatorString(ElementLocatorText locatorText);

    Long getElementIDByLocator(ElementLocator elementLocator);

    Locator getElementLocator(Long elementID);

    Optional<Long> findPromotionTargetElementId(ElementPromotionTarget elementPromotionTarget);

    record ElementCreation(Long applicationId, Long stageId, ElementDefinition elementDefinition) {
    }

    record ElementUpdate(Long applicationId, Long stageId, Long elementId, ElementDefinition elementDefinition) {
    }

    record ElementReference(Long applicationId, Long stageId, Long elementId) {
    }

    record ElementDetails(Long applicationId, Long stageId, Long elementId, boolean includeRetired) {
    }

    record ElementPromotion(Long applicationId, Long sourceStageId, Long elementId, Long targetStageId) {
    }

    record ElementPromotionTarget(Long applicationId, Long sourceElementId, Long targetStageId) {
    }

    record ElementPage(Long applicationId, Long stageId, Integer page, Integer size, boolean includeRetired) {
    }

    record StageReference(Long applicationId, Long stageId) {
    }

    record CandidateStages(Long applicationId, Collection<Long> stageIds) {

        public CandidateStages {
            stageIds = List.copyOf(stageIds);
        }

    }

    record ElementLabel(Long applicationId, Long stageId, String label) {
    }

    record ElementLocatorText(Long applicationId, Long stageId, String locatorString) {
    }

    record ElementLocator(Long applicationId, Long stageId, String locator) {
    }

}
