package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import io.github.yaforster.trails.core.test.ActionGraphLimits;
import io.github.yaforster.trails.core.test.ActionGraphValidationException;
import io.github.yaforster.trails.core.test.ValidatedActionGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Validates the immutable Action graph submitted as a test-plan definition. */
final class TestPlanActionGraphRequestValidator {
    private final ActionGraphLimits limits;

    TestPlanActionGraphRequestValidator() {
        this(ActionGraphLimits.DEFAULTS);
    }

    TestPlanActionGraphRequestValidator(ActionGraphLimits limits) {
        this.limits = limits;
    }

    void validate(TestPlanDefinitionDTO definition, List<ValidationViolation> violations) {
        if (definition == null || definition.getTestSteps() == null) {
            violations.add(new ValidationViolation("TEST_PLAN_TEST_STEPS_NULL",
                    "Test plan test steps must not be null.", "/testSteps"));
            return;
        }
        if (definition.getTestSteps()
                      .isEmpty()) {
            violations.add(new ValidationViolation("TEST_PLAN_TEST_STEPS_EMPTY",
                    "Test plan must define at least one test step.", "/testSteps"));
            return;
        }

        Map<Long, IndexedAction> actionsByReferenceId = indexActions(definition.getTestSteps(), violations);
        if (actionsByReferenceId.size() != definition.getTestSteps()
                                                     .size()) {
            return;
        }
        int violationsBeforeReferences = violations.size();
        Map<Long, List<Long>> nextReferences = validateNextReferences(actionsByReferenceId, violations);
        if (violations.size() > violationsBeforeReferences) {
            return;
        }
        try {
            ValidatedActionGraph.validate(nextReferences, limits);
        } catch (ActionGraphValidationException exception) {
            ValidationViolation violation = switch (exception.reason()) {
                case STARTING_ACTION -> new ValidationViolation("TEST_PLAN_ACTION_GRAPH_STARTING_ACTION_COUNT_INVALID",
                        "Test plan action graph must have exactly one starting action.", "/testSteps");
                case CYCLE -> new ValidationViolation("TEST_PLAN_ACTION_GRAPH_CYCLIC",
                        "Test plan action graph must not contain cycles.", "/testSteps");
                case ACTION_COUNT, PATH_COUNT, ACTION_OCCURRENCES -> new ValidationViolation(
                        "TEST_PLAN_ACTION_GRAPH_LIMIT_EXCEEDED", exception.getMessage(), "/testSteps");
                case IDENTIFIER, SUCCESSORS, UNKNOWN_SUCCESSOR ->
                    throw new IllegalStateException("Transport accepted an invalid action reference graph", exception);
            };
            boolean structuralFailure = switch (exception.reason()) {
                case ACTION_COUNT, PATH_COUNT, ACTION_OCCURRENCES -> false;
                default -> true;
            };
            if (structuralFailure || violations.isEmpty()) {
                violations.add(violation);
            }
        }
    }

    private Map<Long, IndexedAction> indexActions(List<ActionDefinitionDTO> actions,
            List<ValidationViolation> violations) {
        Map<Long, IndexedAction> actionsByReferenceId = new LinkedHashMap<>();
        for (int index = 0; index < actions.size(); index++) {
            ActionDefinitionDTO action = actions.get(index);
            String path = "/testSteps/" + index;
            if (action == null) {
                violations.add(
                        new ValidationViolation("TEST_PLAN_ACTION_NULL", "Test plan action must not be null.", path));
                continue;
            }
            if (action.getReferenceID() == null) {
                violations.add(new ValidationViolation("TEST_PLAN_ACTION_REFERENCE_ID_NULL",
                        "Test plan action reference ID must not be null.", path + "/referenceID"));
                continue;
            }
            if (action.getDetails() == null) {
                violations.add(new ValidationViolation("TEST_PLAN_ACTION_DETAILS_NULL",
                        "Test plan action details must not be null.", path + "/details"));
            }
            if (actionsByReferenceId.putIfAbsent(action.getReferenceID(), new IndexedAction(action, index)) != null) {
                violations.add(new ValidationViolation("TEST_PLAN_ACTION_REFERENCE_ID_DUPLICATE",
                        "Test plan action reference IDs must be unique.", path + "/referenceID"));
            }
        }
        return actionsByReferenceId;
    }

    private Map<Long, List<Long>> validateNextReferences(Map<Long, IndexedAction> actions,
            List<ValidationViolation> violations) {
        Map<Long, List<Long>> nextReferences = new LinkedHashMap<>();
        for (Map.Entry<Long, IndexedAction> entry : actions.entrySet()) {
            List<Long> nextActionIds = Objects.requireNonNullElse(entry.getValue()
                                                                       .action()
                                                                       .getNextActions(),
                    List.of());
            nextReferences.put(entry.getKey(), new ArrayList<>(nextActionIds));
            Set<Long> seen = new HashSet<>();
            for (int index = 0; index < nextActionIds.size(); index++) {
                Long nextActionId = nextActionIds.get(index);
                String path = "/testSteps/" + entry.getValue()
                                                   .index()
                        + "/nextActions/" + index;
                if (nextActionId == null) {
                    violations.add(new ValidationViolation("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_NULL",
                            "Next action reference ID must not be null.", path));
                } else if (!seen.add(nextActionId)) {
                    violations.add(new ValidationViolation("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_DUPLICATE",
                            "Next action reference IDs must be unique per action.", path));
                } else if (!actions.containsKey(nextActionId)) {
                    violations.add(new ValidationViolation("TEST_PLAN_NEXT_ACTION_REFERENCE_ID_UNKNOWN",
                            "Next action reference ID must identify an action in the test plan.", path));
                }
            }
        }
        return nextReferences;
    }

}
