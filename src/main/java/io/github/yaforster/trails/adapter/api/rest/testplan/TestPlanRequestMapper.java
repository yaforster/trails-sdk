package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionElementReferences;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanGroupDTO;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.definition.TestPlanGroup;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/** Converts a validated test-plan request to its framework-free domain definition. */
final class TestPlanRequestMapper {

    private final ActionMapper actions;

    TestPlanRequestMapper(ActionMapper actions) {
        this.actions = Objects.requireNonNull(actions, "Action mapper is required");
    }

    TestPlanDefinition definition(Long applicationId, Long stageId, TestPlanDefinitionDTO request,
            ActionElementReferences elements) {
        return new TestPlanDefinition(applicationId, stageId, request.getLabel(), request.getTestSteps()
                                                                                         .stream()
                                                                                         .map(action -> actions.fromDefinitionDTO(
                                                                                                 action, elements))
                                                                                         .toList(),
                groups(request.getGroups()));
    }

    private List<TestPlanGroup> groups(List<TestPlanGroupDTO> groups) {
        if (groups == null) {
            return List.of();
        }
        return groups.stream()
                     .map(this::group)
                     .toList();
    }

    private TestPlanGroup group(TestPlanGroupDTO group) {
        List<Long> actionReferences = Objects.requireNonNullElse(group.getActionReferenceIds(), List.of());
        return new TestPlanGroup(group.getLabel(), BigDecimal.valueOf(group.getxCoordinate()),
                BigDecimal.valueOf(group.getyCoordinate()), BigDecimal.valueOf(group.getWidthPixels()),
                BigDecimal.valueOf(group.getHeightPixels()), List.copyOf(actionReferences));
    }

}
