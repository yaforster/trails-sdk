package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDefinitionDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

/** Validates element transport input before it crosses into the domain. */
final class ElementRequestValidator {

    private final ElementDatabaseService elements;

    ElementRequestValidator(ElementDatabaseService elements) {
        this.elements = Objects.requireNonNull(elements, "Element database service is required");
    }

    void validateCreate(Long applicationId, Long stageId, ElementDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        validateDefinition(request, violations);
        validateUniqueCreateFields(applicationId, stageId, request, violations);
        rejectIfInvalid(violations);
    }

    void validateUpdate(Long applicationId, Long stageId, Long elementId, ElementDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateElementReference(applicationId, stageId, elementId, violations);
        validateDefinition(request, violations);
        rejectIfInvalid(violations);
    }

    void validateElementReference(Long applicationId, Long stageId, Long elementId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateElementReference(applicationId, stageId, elementId, violations);
        rejectIfInvalid(violations);
    }

    void validatePage(Long applicationId, Long stageId, Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateStageReference(applicationId, stageId, violations);
        if (page == null) {
            violations.add(new ValidationViolation("ELEMENT_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("ELEMENT_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("ELEMENT_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("ELEMENT_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    void validateUpload(Long applicationId, Long stageId, Long elementId, MultipartFile file) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateElementReference(applicationId, stageId, elementId, violations);
        if (file == null) {
            violations.add(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_NULL",
                    "Element screenshot file must not be null.", "/file"));
        }
        rejectIfInvalid(violations);
    }

    void validatePromotion(Long applicationId, Long sourceStageId, Long elementId, Long targetStageId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateElementReference(applicationId, sourceStageId, elementId, violations);
        if (targetStageId == null) {
            violations.add(new ValidationViolation("ELEMENT_TARGET_STAGE_ID_NULL", "Target stage id must not be null.",
                    "/targetStageId"));
        }
        if (sourceStageId != null && sourceStageId.equals(targetStageId)) {
            violations.add(new ValidationViolation("ELEMENT_PROMOTE_TARGET_STAGE_EQUALS_SOURCE_STAGE",
                    "Target stage must be different from source stage.", "/targetStageId"));
        }
        rejectIfInvalid(violations);
    }

    private void validateDefinition(ElementDefinitionDTO request, List<ValidationViolation> violations) {
        if (request == null || request.getType() == null) {
            violations.add(new ValidationViolation("ELEMENT_TYPE_NULL", "Element type must not be null.", "/type"));
        }
        if (request == null || request.getLabel() == null || request.getLabel()
                                                                    .isBlank()) {
            violations.add(
                    new ValidationViolation("ELEMENT_LABEL_EMPTY", "Element label must not be empty.", "/label"));
        }
        if (request == null || request.getLocatorString() == null || request.getLocatorString()
                                                                            .isBlank()) {
            violations.add(new ValidationViolation("ELEMENT_LOCATOR_STRING_EMPTY",
                    "Element locator string must not be empty.", "/locatorString"));
        }
        if (request == null || request.getLocatorType() == null) {
            violations.add(new ValidationViolation("ELEMENT_LOCATOR_TYPE_NULL",
                    "Element locator type must not be null.", "/locatorType"));
        }
    }

    private void validateUniqueCreateFields(Long applicationId, Long stageId, ElementDefinitionDTO request,
            List<ValidationViolation> violations) {
        if (applicationId == null || stageId == null || request == null) {
            return;
        }
        if (request.getLabel() != null && !request.getLabel()
                                                  .isBlank()
                && elements.existsByLabel(
                        new ElementDatabaseService.ElementLabel(applicationId, stageId, request.getLabel()))) {
            violations.add(new ValidationViolation("ELEMENT_LABEL_ALREADY_EXISTS",
                    "Element label must be unique within the stage.", "/label"));
        }
        if (request.getLocatorString() != null && !request.getLocatorString()
                                                          .isBlank()
                && elements.existsByLocatorString(new ElementDatabaseService.ElementLocatorText(applicationId, stageId,
                        request.getLocatorString()))) {
            violations.add(new ValidationViolation("ELEMENT_LOCATOR_STRING_ALREADY_EXISTS",
                    "Element locator string must be unique within the stage.", "/locatorString"));
        }
    }

    private void validateElementReference(Long applicationId, Long stageId, Long elementId,
            List<ValidationViolation> violations) {
        validateStageReference(applicationId, stageId, violations);
        if (elementId == null) {
            violations.add(new ValidationViolation("ELEMENT_ID_NULL", "Element id must not be null.", "/elementId"));
        }
    }

    private void validateStageReference(Long applicationId, Long stageId, List<ValidationViolation> violations) {
        if (applicationId == null) {
            violations.add(new ValidationViolation("ELEMENT_APPLICATION_ID_NULL", "Application id must not be null.",
                    "/applicationId"));
        }
        if (stageId == null) {
            violations.add(new ValidationViolation("ELEMENT_STAGE_ID_NULL", "Stage id must not be null.", "/stageId"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
