package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;

/** Validates deployment transport input before it crosses into the domain. */
final class DeploymentRequestValidator {

    void validateReport(Long applicationId, Long stageId, DeploymentDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        if (request == null || request.getVersion() == null || request.getVersion()
                                                                      .isBlank()) {
            violations.add(new ValidationViolation("DEPLOYMENT_VERSION_EMPTY", "Deployment version must not be empty.",
                    "/version"));
        }
        if (request == null || request.getDeployedAt() == null) {
            violations.add(new ValidationViolation("DEPLOYMENT_TIMESTAMP_NULL",
                    "Deployment timestamp must not be null.", "/deployedAt"));
        }
        rejectIfInvalid(violations);
    }

    void validateDeploymentReference(Long applicationId, Long stageId, Long deploymentId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        if (deploymentId == null) {
            violations.add(
                    new ValidationViolation("DEPLOYMENT_ID_NULL", "Deployment id must not be null.", "/deploymentId"));
        }
        rejectIfInvalid(violations);
    }

    void validatePage(Long applicationId, Long stageId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        if (page == null || page < 0) {
            violations.add(
                    new ValidationViolation("PAGE_INVALID", "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null || size < 1 || size > 100) {
            violations.add(new ValidationViolation("SIZE_INVALID", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    private void validateStageReference(Long applicationId, Long stageId, List<ValidationViolation> violations) {
        if (applicationId == null) {
            violations.add(new ValidationViolation("DEPLOYMENT_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(
                    new ValidationViolation("DEPLOYMENT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
