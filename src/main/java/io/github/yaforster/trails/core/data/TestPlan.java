package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.definition.TestPlanGroup;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TestPlan {

    private Long id;
    private Long applicationID;
    private Long stageID;
    private String label;
    private List<Action> actions;
    private List<TestPlanGroup> groups;

    public TestPlan(Long id, Long applicationID, Long stageID, String label, List<Action> actions) {
        this(id, applicationID, stageID, label, actions, List.of());
    }

}
