package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates Action collection references and paging before querying persistence. */
final class ActionRequestValidator {

    void validatePage(Long applicationId, Long stageId, Long testPlanId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateId(applicationId, "ACTION_APPLICATION_ID_NULL", "Application id must not be null.", "/applicationId",
                violations);
        validateId(stageId, "ACTION_STAGE_ID_NULL", "Stage id must not be null.", "/stageId", violations);
        validateId(testPlanId, "ACTION_TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanId", violations);
        validatePageNumber(page, violations);
        validatePageSize(size, violations);
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

    private void validateId(Long value, String code, String message, String path,
            List<ValidationViolation> violations) {
        if (value == null) {
            violations.add(new ValidationViolation(code, message, path));
        }
    }

    private void validatePageNumber(Integer page, List<ValidationViolation> violations) {
        if (page == null) {
            violations.add(new ValidationViolation("ACTION_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("ACTION_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
    }

    private void validatePageSize(Integer size, List<ValidationViolation> violations) {
        if (size == null) {
            violations.add(new ValidationViolation("ACTION_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("ACTION_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
    }

}
