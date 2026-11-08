package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates identifiers used to request a PDF representation of a persisted result. */
final class PrintRequestValidator {

    void testRun(Long applicationId, Long stageId, Long testRunId) {
        rejectIfInvalid(testRunViolations(applicationId, stageId, testRunId));
    }

    void testSet(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
        List<ValidationViolation> violations = testRunViolations(applicationId, stageId, testRunId);
        if (testSetResultId == null) {
            violations.add(new ValidationViolation("PRINT_TEST_SET_RESULT_ID_NULL",
                    "Test set result id must not be null.", "/testSetResultId"));
        }
        rejectIfInvalid(violations);
    }

    void path(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
        List<ValidationViolation> violations = testRunViolations(applicationId, stageId, testRunId);
        if (testSetResultId == null) {
            violations.add(new ValidationViolation("PRINT_TEST_SET_RESULT_ID_NULL",
                    "Test set result id must not be null.", "/testSetResultId"));
        }
        if (pathResultId == null) {
            violations.add(new ValidationViolation("PRINT_PATH_RESULT_ID_NULL", "Path result id must not be null.",
                    "/pathResultId"));
        }
        rejectIfInvalid(violations);
    }

    private List<ValidationViolation> testRunViolations(Long applicationId, Long stageId, Long testRunId) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (applicationId == null) {
            violations.add(new ValidationViolation("PRINT_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(new ValidationViolation("PRINT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
        if (testRunId == null) {
            violations.add(
                    new ValidationViolation("PRINT_TEST_RUN_ID_NULL", "Test run id must not be null.", "/testRunId"));
        }
        return violations;
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
