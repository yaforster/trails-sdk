package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.data.ResultIndicator;

import java.sql.Timestamp;

public record PersistedTestRunResult(Long id, Long applicationId, Long stageId, Long testPlanId, Timestamp timestamp,
        ResultIndicator status, String label) {

}
