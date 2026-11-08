package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates the complete parent hierarchy for an action-result chain request. */
final class ActionResultRequestValidator {

    void validate(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (applicationId == null) {
            violations.add(new ValidationViolation("ACTION_RESULT_APPLICATION_ID_NULL",
                    "Application id must not be null.", "/applicationId"));
        }
        if (stageId == null) {
            violations.add(
                    new ValidationViolation("ACTION_RESULT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
        if (testRunId == null) {
            violations.add(new ValidationViolation("ACTION_RESULT_TEST_RUN_ID_NULL", "Test run id must not be null.",
                    "/testRunId"));
        }
        if (testSetResultId == null) {
            violations.add(new ValidationViolation("ACTION_RESULT_TEST_SET_RESULT_ID_NULL",
                    "Test set result id must not be null.", "/testSetResultId"));
        }
        if (pathResultId == null) {
            violations.add(new ValidationViolation("ACTION_RESULT_PATH_RESULT_ID_NULL",
                    "Path result id must not be null.", "/pathResultId"));
        }
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
