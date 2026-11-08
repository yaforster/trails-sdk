package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.data.ResultIndicator;

import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

public record TestRunResult(List<TestSetResult> testResults, Timestamp timestamp, ResultIndicator indicator,
        Long applicationID, Long stageID, Long testPlanID, String label) {

    public TestRunResult {
        testResults = List.copyOf(Objects.requireNonNull(testResults, "Test set results are required"));
        Objects.requireNonNull(timestamp, "Test run timestamp is required");
        timestamp = Timestamp.from(timestamp.toInstant());
        Objects.requireNonNull(indicator, "Test run indicator is required");
        Objects.requireNonNull(applicationID, "Test run application identifier is required");
        Objects.requireNonNull(stageID, "Test run stage identifier is required");
        Objects.requireNonNull(testPlanID, "Test plan identifier is required");
        Objects.requireNonNull(label, "Test plan label is required");
    }

    @Override
    public Timestamp timestamp() {
        return Timestamp.from(timestamp.toInstant());
    }

}
