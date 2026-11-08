package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import io.github.yaforster.trails.core.test.TestRunResult;

import java.util.Optional;

public interface TestRunResultDatabaseService {

    record TestRunResultDetails(Long applicationId, Long stageId, Long testRunId) {
    }

    record TestRunReference(Long testRunId) {
    }

    record TestRunPage(Long applicationId, Long stageId, int page, int size) {
    }

    record TestPlanStatistics(Long applicationId, Long stageId, Long testPlanId) {
    }

    record TestPlanHistory(Long applicationId, Long stageId, Long testPlanId, int page, int size) {
    }

    Optional<PersistedTestRunResult> getTestRunResult(TestRunResultDetails testRunResultDetails);

    Optional<PersistedTestRunResult> getTestRunResult(TestRunReference testRunReference);

    Optional<PagedResult<PersistedTestRunResult>> listTestRuns(TestRunPage testRunPage);

    Optional<PersistedTestRunStatistics> getTestRunStatistics(TestPlanStatistics testPlanStatistics);

    Optional<PersistedTestRunHistory> getTestRunHistory(TestPlanHistory testPlanHistory);

    PersistedTestRunResult storeTestRunResult(TestRunResult result);

}
