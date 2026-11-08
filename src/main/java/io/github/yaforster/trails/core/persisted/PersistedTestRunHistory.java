package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.PagedResult;

public record PersistedTestRunHistory(Long applicationId, Long stageId, Long testPlanId, long totalRuns,
        long successfulRuns, long partialSuccessRuns, long failedRuns, PagedResult<PersistedTestRunResult> items) {
}
