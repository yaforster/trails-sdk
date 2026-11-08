package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.definition.TestPlanGroup;
import java.util.List;

public record PersistedTestPlan(Long id, Long applicationID, Long stageID, String label, boolean retired,
        List<TestPlanGroup> groups) {

    public PersistedTestPlan(Long id, Long applicationID, Long stageID, String label) {
        this(id, applicationID, stageID, label, false, List.of());
    }

    public PersistedTestPlan(Long id, Long applicationID, Long stageID, String label, boolean retired) {
        this(id, applicationID, stageID, label, retired, List.of());
    }
}
