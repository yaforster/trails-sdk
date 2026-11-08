package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Validates test-plan resource requests before they cross the transport boundary. */
final class TestPlanRequestValidator {

    private final TestPlanDatabaseService testPlans;
    private final TestPlanActionGraphRequestValidator actionGraphs;

    TestPlanRequestValidator(TestPlanDatabaseService testPlans, TestPlanActionGraphRequestValidator actionGraphs) {
        this.testPlans = Objects.requireNonNull(testPlans, "Test plan database service is required");
        this.actionGraphs = Objects.requireNonNull(actionGraphs, "Test plan action graph validator is required");
    }

    void validateCreate(Long applicationId, Long stageId, TestPlanDefinitionDTO definition) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        validateLabel(definition, violations);
        actionGraphs.validate(definition, violations);
        validateUniqueLabel(applicationId, stageId, definition, violations);
        rejectIfInvalid(violations);
    }

    void validateTestPlanReference(Long applicationId, Long stageId, Long testPlanId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        if (testPlanId == null) {
            violations.add(
                    new ValidationViolation("TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
        }
        rejectIfInvalid(violations);
    }

    void validatePage(Long applicationId, Long stageId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        if (page == null) {
            violations.add(new ValidationViolation("TEST_PLAN_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("TEST_PLAN_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("TEST_PLAN_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("TEST_PLAN_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    void validatePromotion(Long applicationId, Long sourceStageId, Long testPlanId, Long targetStageId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, sourceStageId, violations);
        if (testPlanId == null) {
            violations.add(
                    new ValidationViolation("TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId"));
        }
        if (targetStageId == null) {
            violations.add(new ValidationViolation("TEST_PLAN_TARGET_STAGE_ID_NULL",
                    "Target stage id must not be null.", "/targetStageId"));
        } else if (targetStageId.equals(sourceStageId)) {
            violations.add(new ValidationViolation("TEST_PLAN_PROMOTE_TARGET_STAGE_EQUALS_SOURCE_STAGE",
                    "Target stage must be different from source stage.", "/targetStageId"));
        }
        rejectIfInvalid(violations);
    }

    private void validateStageReference(Long applicationId, Long stageId, List<ValidationViolation> violations) {
        if (applicationId == null) {
            violations.add(new ValidationViolation("TEST_PLAN_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(
                    new ValidationViolation("TEST_PLAN_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
    }

    private void validateLabel(TestPlanDefinitionDTO definition, List<ValidationViolation> violations) {
        if (definition == null || definition.getLabel() == null || definition.getLabel()
                                                                             .isBlank()) {
            violations.add(
                    new ValidationViolation("TEST_PLAN_LABEL_EMPTY", "Test plan label must not be empty.", "/label"));
        }
    }

    private void validateUniqueLabel(Long applicationId, Long stageId, TestPlanDefinitionDTO definition,
            List<ValidationViolation> violations) {
        if (applicationId != null && stageId != null && definition != null && definition.getLabel() != null
                && !definition.getLabel()
                              .isBlank()
                && testPlans.existsByLabel(
                        new TestPlanDatabaseService.TestPlanLabel(applicationId, stageId, definition.getLabel()))) {
            violations.add(new ValidationViolation("TEST_PLAN_LABEL_ALREADY_EXISTS",
                    "Test plan label must be unique within the stage.", "/label"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
