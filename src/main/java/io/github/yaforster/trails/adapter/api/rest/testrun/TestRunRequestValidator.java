package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates the identifiers and paging arguments accepted by the test-run resource. */
final class TestRunRequestValidator {

    void validateTestRun(Long testRunId, Long applicationId, Long stageId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationAndStage(applicationId, stageId, violations);
        if (testRunId == null) {
            violations.add(new ValidationViolation("TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
        }
        rejectIfInvalid(violations);
    }

    void validatePage(Long applicationId, Long stageId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationAndStage(applicationId, stageId, violations);
        validatePage(page, size, "TEST_RUN", violations);
        rejectIfInvalid(violations);
    }

    void validateTestPlan(Long applicationId, Long stageId, Long testPlanId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationAndStage(applicationId, stageId, violations);
        validateTestPlanId(testPlanId, violations);
        rejectIfInvalid(violations);
    }

    void validateHistory(Long applicationId, Long stageId, Long testPlanId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationAndStage(applicationId, stageId, violations);
        validateTestPlanId(testPlanId, violations);
        validatePage(page, size, "TEST_RUN_HISTORY", violations);
        rejectIfInvalid(violations);
    }

    private void validateApplicationAndStage(Long applicationId, Long stageId, List<ValidationViolation> violations) {
        if (applicationId == null) {
            violations.add(new ValidationViolation("TEST_RUN_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(new ValidationViolation("TEST_RUN_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
    }

    private void validateTestPlanId(Long testPlanId, List<ValidationViolation> violations) {
        if (testPlanId == null) {
            violations.add(new ValidationViolation("TEST_RUN_TEST_PLAN_ID_NULL", "Test plan id must not be null.",
                    "/testPlanId"));
        }
    }

    private void validatePage(Integer page, Integer size, String resource, List<ValidationViolation> violations) {
        if (page == null) {
            violations.add(new ValidationViolation(resource + "_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation(resource + "_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation(resource + "_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(new ValidationViolation(resource + "_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.",
                    "/size"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
