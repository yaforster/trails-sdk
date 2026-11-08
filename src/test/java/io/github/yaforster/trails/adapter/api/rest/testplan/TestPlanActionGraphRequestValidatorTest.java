package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.SwitchWebsiteDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestPlanActionGraphRequestValidatorTest {
    @Test
    void shouldReportConfiguredLimits_whenDefinitionWouldExpandBeyondThem() {
        ActionDefinitionDTO first = action(1L, 2L).details(new SwitchWebsiteDetailsDTO());
        ActionDefinitionDTO second = action(2L).details(new SwitchWebsiteDetailsDTO());
        List<ValidationViolation> violations = new ArrayList<>();
        TestPlanActionGraphRequestValidator validator = new TestPlanActionGraphRequestValidator(
                new io.github.yaforster.trails.core.test.ActionGraphLimits(2, 1, 1));
        validator.validate(definition(first, second), violations);
        assertThat(violations).extracting(ValidationViolation::code)
                              .containsExactly("TEST_PLAN_ACTION_GRAPH_LIMIT_EXCEEDED");
    }

    @Test
    void shouldRejectCyclicActionGraph() {
        List<ValidationViolation> violations = validate(definition(action(1L, 2L), action(2L, 1L)));

        assertThat(violations).extracting(ValidationViolation::code)
                              .containsExactly("TEST_PLAN_ACTION_DETAILS_NULL", "TEST_PLAN_ACTION_DETAILS_NULL",
                                      "TEST_PLAN_ACTION_GRAPH_CYCLIC");
    }

    @Test
    void shouldRejectMissingActionDetailsBeforeDomainMapping() {
        List<ValidationViolation> violations = validate(definition(action(1L)));

        assertThat(violations).extracting(ValidationViolation::code)
                              .containsExactly("TEST_PLAN_ACTION_DETAILS_NULL");
    }

    @Test
    void shouldRejectDuplicateAndUnknownNextActionReferences() {
        List<ValidationViolation> violations = validate(definition(action(1L, 2L, 2L, 3L), action(2L)));

        assertThat(violations).extracting(ValidationViolation::code)
                              .containsExactly("TEST_PLAN_ACTION_DETAILS_NULL", "TEST_PLAN_ACTION_DETAILS_NULL",
                                      "TEST_PLAN_NEXT_ACTION_REFERENCE_ID_DUPLICATE",
                                      "TEST_PLAN_NEXT_ACTION_REFERENCE_ID_UNKNOWN");
    }

    @Test
    void shouldAcceptAcyclicGraphWithOneStartingActionWhenDetailsAreProvided() {
        ActionDefinitionDTO first = action(1L, 2L);
        ActionDefinitionDTO second = action(2L);
        first.setDetails(new SwitchWebsiteDetailsDTO());
        second.setDetails(new SwitchWebsiteDetailsDTO());

        assertThat(validate(definition(first, second))).isEmpty();
    }

    private List<ValidationViolation> validate(TestPlanDefinitionDTO definition) {
        List<ValidationViolation> violations = new ArrayList<>();
        new TestPlanActionGraphRequestValidator().validate(definition, violations);
        return violations;
    }

    private TestPlanDefinitionDTO definition(ActionDefinitionDTO... actions) {
        return new TestPlanDefinitionDTO().testSteps(List.of(actions));
    }

    private ActionDefinitionDTO action(Long referenceId, Long... nextActionReferenceIds) {
        return new ActionDefinitionDTO().referenceID(referenceId)
                                        .nextActions(Arrays.asList(nextActionReferenceIds));
    }

}
