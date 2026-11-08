package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TestPlanDatabaseService {

    PersistedTestPlan storeTestPlan(TestPlanDefinition definition);

    DatabaseDeletionResult deleteTestPlan(TestPlanReference testPlanReference);

    DatabaseDeletionResult restoreTestPlan(TestPlanReference testPlanReference);

    Optional<PersistedTestPlan> promoteTestPlan(TestPlanPromotion testPlanPromotion);

    Optional<TestPlan> getTestPlan(TestPlanReference testPlanReference);

    Optional<PersistedTestPlan> getPersistedTestPlan(PersistedTestPlanDetails persistedTestPlanDetails);

    PagedResult<PersistedTestPlan> getTestPlans(TestPlanPage testPlanPage);

    boolean hasActions(Long testPlanId);

    Set<Long> findTestPlanIdsWithActions(TestPlanActions testPlanActions);

    boolean existsByLabel(TestPlanLabel testPlanLabel);

    Optional<TestPlan> getExecutableTestPlan(Long testPlanId);

    record TestPlanReference(Long applicationId, Long stageId, Long testPlanId) {
    }

    record TestPlanPromotion(Long applicationId, Long sourceStageId, Long testPlanId, Long targetStageId) {
    }

    record PersistedTestPlanDetails(Long applicationId, Long stageId, Long testPlanId, boolean includeRetired) {
    }

    record TestPlanPage(Long applicationId, Long stageId, Integer page, Integer size, boolean includeRetired) {
    }

    record TestPlanActions(Long applicationId, Long stageId, Collection<Long> testPlanIds) {

        public TestPlanActions {
            testPlanIds = List.copyOf(testPlanIds);
        }

    }

    record TestPlanLabel(Long applicationId, Long stageId, String label) {
    }

}
