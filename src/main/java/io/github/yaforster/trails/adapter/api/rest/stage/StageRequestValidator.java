package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.StageDefinitionDTO;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates stage transport input before it crosses into the domain.
 */
final class StageRequestValidator {

    private final StageDatabaseService stages;

    StageRequestValidator(StageDatabaseService stages) {
        this.stages = Objects.requireNonNull(stages, "Stage database service is required");
    }

    void validateCreate(Long applicationId, StageDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationId(applicationId, violations);
        if (request == null || request.getLabel() == null || request.getLabel()
                                                                    .isBlank()) {
            violations.add(new ValidationViolation("STAGE_LABEL_EMPTY", "Stage label must not be empty.", "/label"));
        }
        if (request == null || request.getUrl() == null) {
            violations.add(new ValidationViolation("STAGE_URL_EMPTY", "Stage url must not be empty.", "/url"));
        }
        if (applicationId != null && request != null && request.getLabel() != null && !request.getLabel()
                                                                                              .isBlank()
                && stages.existsByLabel(new StageDatabaseService.StageLabel(applicationId, request.getLabel()))) {
            violations.add(new ValidationViolation("STAGE_LABEL_ALREADY_EXISTS",
                    "Stage label must be unique within the application.", "/label"));
        }
        if (applicationId != null && request != null && request.getUrl() != null
                && stages.existsByUrl(new StageDatabaseService.StageUrl(applicationId, request.getUrl()
                                                                                              .toString()))) {
            violations.add(new ValidationViolation("STAGE_URL_ALREADY_EXISTS",
                    "Stage url must be unique within the application.", "/url"));
        }
        rejectIfInvalid(violations);
    }

    void validateStageReference(Long applicationId, Long stageId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationId(applicationId, violations);
        if (stageId == null) {
            violations.add(new ValidationViolation("STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
        rejectIfInvalid(violations);
    }

    void validatePage(Long applicationId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateApplicationId(applicationId, violations);
        if (page == null) {
            violations.add(new ValidationViolation("STAGE_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("STAGE_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("STAGE_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("STAGE_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    private void validateApplicationId(Long applicationId, List<ValidationViolation> violations) {
        if (applicationId == null) {
            violations.add(new ValidationViolation("STAGE_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
