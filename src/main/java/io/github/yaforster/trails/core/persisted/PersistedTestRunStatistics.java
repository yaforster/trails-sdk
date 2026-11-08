package io.github.yaforster.trails.core.persisted;

public record PersistedTestRunStatistics(Long applicationId, Long stageId, Long testPlanId, long totalRuns,
        long successfulRuns, long partialSuccessRuns, long failedRuns) {

}
