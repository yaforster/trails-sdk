package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates artifact request identifiers and page bounds before querying binary data. */
final class ArtifactRequestValidator {

    void validateArtifact(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId,
            Long artifactId) {
        List<ValidationViolation> violations = hierarchyViolations(applicationId, stageId, testRunId, testSetResultId,
                pathResultId);
        if (artifactId == null) {
            violations.add(new ValidationViolation("ARTIFACT_ID_NULL", "Artifact id must not be null.", "/artifactId"));
        }
        rejectIfInvalid(violations);
    }

    void validateScreenshot(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId,
            Long actionResultId) {
        List<ValidationViolation> violations = hierarchyViolations(applicationId, stageId, testRunId, testSetResultId,
                pathResultId);
        if (actionResultId == null) {
            violations.add(new ValidationViolation("ARTIFACT_ACTION_RESULT_ID_NULL",
                    "Action result id must not be null.", "/actionResultId"));
        }
        rejectIfInvalid(violations);
    }

    void validateList(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId,
            Integer page, Integer size) {
        List<ValidationViolation> violations = hierarchyViolations(applicationId, stageId, testRunId, testSetResultId,
                pathResultId);
        if (page == null) {
            violations.add(new ValidationViolation("ARTIFACT_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("ARTIFACT_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("ARTIFACT_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("ARTIFACT_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    private List<ValidationViolation> hierarchyViolations(Long applicationId, Long stageId, Long testRunId,
            Long testSetResultId, Long pathResultId) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (applicationId == null) {
            violations.add(new ValidationViolation("ARTIFACT_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(new ValidationViolation("ARTIFACT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
        if (testRunId == null) {
            violations.add(new ValidationViolation("ARTIFACT_TEST_RUN_ID_NULL", "Test run id must not be null.",
                    "/testRunId"));
        }
        if (testSetResultId == null) {
            violations.add(new ValidationViolation("ARTIFACT_TEST_SET_RESULT_ID_NULL",
                    "Test set result id must not be null.", "/testSetResultId"));
        }
        if (pathResultId == null) {
            violations.add(new ValidationViolation("ARTIFACT_PATH_RESULT_ID_NULL", "Path result id must not be null.",
                    "/pathResultId"));
        }
        return violations;
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
