package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDefinitionDTO;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Validates application transport input before it crosses into the domain. */
final class ApplicationRequestValidator {

    private final ApplicationDatabaseService applications;

    ApplicationRequestValidator(ApplicationDatabaseService applications) {
        this.applications = Objects.requireNonNull(applications, "Application database service is required");
    }

    void validateCreate(ApplicationDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (request == null || request.getLabel() == null || request.getLabel()
                                                                    .isBlank()) {
            violations.add(new ValidationViolation("APPLICATION_LABEL_EMPTY", "Application label must not be empty.",
                    "/label"));
        } else if (applications.existsByLabel(request.getLabel())) {
            violations.add(new ValidationViolation("APPLICATION_LABEL_ALREADY_EXISTS",
                    "Application label must be unique.", "/label"));
        }
        rejectIfInvalid(violations);
    }

    void validateApplicationId(Long applicationId) {
        if (applicationId == null) {
            throw new ApiRequestValidationException(List.of(new ValidationViolation("APPLICATION_ID_NULL",
                    "Application ID must not be null.", "/applicationId")));
        }
    }

    void validatePage(Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (page == null) {
            violations.add(new ValidationViolation("APPLICATION_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("APPLICATION_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("APPLICATION_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(new ValidationViolation("APPLICATION_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.",
                    "/size"));
        }
        rejectIfInvalid(violations);
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
