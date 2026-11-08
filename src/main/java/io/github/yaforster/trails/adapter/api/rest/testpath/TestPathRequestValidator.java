package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates the complete browser-result hierarchy named by a test-path request. */
final class TestPathRequestValidator {

    void validatePathResults(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Integer page,
            Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateTestSet(applicationId, stageId, testRunId, testSetResultId, violations);
        validatePage(page, size, violations);
        rejectIfInvalid(violations);
    }

    void validatePathResult(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateTestSet(applicationId, stageId, testRunId, testSetResultId, violations);
        if (pathResultId == null) {
            violations.add(new ValidationViolation("TEST_PATH_RESULT_ID_NULL", "Path result id must not be null.",
                    "/pathResultId"));
        }
        rejectIfInvalid(violations);
    }

    private void validateTestSet(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            List<ValidationViolation> violations) {
        if (applicationId == null) {
            violations.add(new ValidationViolation("TEST_PATH_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(
                    new ValidationViolation("TEST_PATH_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
        if (testRunId == null) {
            violations.add(new ValidationViolation("TEST_PATH_TEST_RUN_ID_NULL", "Test run id must not be null.",
                    "/testRunId"));
        }
        if (testSetResultId == null) {
            violations.add(new ValidationViolation("TEST_PATH_TEST_SET_RESULT_ID_NULL",
                    "Test set result id must not be null.", "/testSetResultId"));
        }
    }

    private void validatePage(Integer page, Integer size, List<ValidationViolation> violations) {
        if (page == null) {
            violations.add(new ValidationViolation("TEST_PATH_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("TEST_PATH_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("TEST_PATH_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("TEST_PATH_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
