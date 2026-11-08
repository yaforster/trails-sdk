package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates browser-result URI identifiers and paging before persistence access. */
final class TestSetRequestValidator {

    void validateBrowserResults(Long testRunId, Long applicationId, Long stageId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateTestRun(testRunId, applicationId, stageId, violations);
        validatePage(page, size, violations);
        rejectIfInvalid(violations);
    }

    void validateBrowserResult(Long testRunId, Long applicationId, Long stageId, Long testSetResultId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateTestRun(testRunId, applicationId, stageId, violations);
        if (testSetResultId == null) {
            violations.add(new ValidationViolation("TEST_SET_RESULT_ID_NULL", "Test set result id must not be null.",
                    "/testSetResultId"));
        }
        rejectIfInvalid(violations);
    }

    private void validateTestRun(Long testRunId, Long applicationId, Long stageId,
            List<ValidationViolation> violations) {
        if (testRunId == null) {
            violations.add(new ValidationViolation("TEST_SET_TEST_RUN_ID_NULL", "Test run id must not be null.",
                    "/testRunId"));
        }
        if (applicationId == null) {
            violations.add(new ValidationViolation("TEST_SET_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(new ValidationViolation("TEST_SET_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
    }

    private void validatePage(Integer page, Integer size, List<ValidationViolation> violations) {
        if (page == null) {
            violations.add(new ValidationViolation("TEST_SET_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("TEST_SET_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("TEST_SET_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("TEST_SET_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
