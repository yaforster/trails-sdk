package io.github.yaforster.trails.core.definition;

import io.github.yaforster.trails.core.test.Action;

import java.util.List;

public record TestPlanDefinition(Long applicationID, Long stageID, String label, List<Action> actions,
        List<TestPlanGroup> groups) {
    public TestPlanDefinition {
        actions = List.copyOf(actions);
        groups = List.copyOf(groups);
    }

}
