package io.github.yaforster.trails.adapter.api.rest;

import io.github.yaforster.trails.adapter.api.rest.action.ActionController;
import io.github.yaforster.trails.adapter.api.rest.actionresult.ActionResultController;
import io.github.yaforster.trails.adapter.api.rest.application.ApplicationController;
import io.github.yaforster.trails.adapter.api.rest.artifact.ArtifactController;
import io.github.yaforster.trails.adapter.api.rest.deployment.DeploymentController;
import io.github.yaforster.trails.adapter.api.rest.element.ElementController;
import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.adapter.api.rest.print.PrintController;
import io.github.yaforster.trails.adapter.api.rest.stage.StageController;
import io.github.yaforster.trails.adapter.api.rest.testdata.TestDataController;
import io.github.yaforster.trails.adapter.api.rest.testpath.TestPathController;
import io.github.yaforster.trails.adapter.api.rest.testplan.ActionDetailsValidationException;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanController;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunController;
import io.github.yaforster.trails.adapter.api.rest.testset.TestSetController;
import io.github.yaforster.trails.adapter.api.rest.user.UserProfileController;
import io.github.yaforster.trails.core.ErrorClassification;
import io.github.yaforster.trails.core.TrailsException;
import io.github.yaforster.trails.core.ValidationViolation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.util.Comparator;
import java.util.List;

/**
 * Maps failures from migrated HATEOAS resources to the established safe contract response.
 */
@RestControllerAdvice(assignableTypes = {ApplicationController.class, ActionController.class, ElementController.class,
        StageController.class, TestDataController.class, DeploymentController.class, TestPlanController.class,
        TestRunController.class, TestSetController.class, TestPathController.class, ActionResultController.class,
        ArtifactController.class, PrintController.class, UserProfileController.class})
public final class ResourceExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ResourceExceptionHandler.class);

    @ExceptionHandler(ApiRequestValidationException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleValidation(ApiRequestValidationException exception) {
        return ResponseEntity.badRequest()
                             .body(toDtos(exception.violations()));
    }

    @ExceptionHandler(ActionDetailsValidationException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleActionDetailsValidation(ActionDetailsValidationException exception) {
        return ResponseEntity.badRequest()
                             .body(toDtos(exception.getValidationViolations()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleConstraintViolation(ConstraintViolationException exception) {
        return ResponseEntity.badRequest()
                             .body(toDtos(exception.getConstraintViolations()
                                                   .stream()
                                                   .sorted(Comparator.comparing(violation -> violation.getPropertyPath()
                                                                                                      .toString()))
                                                   .map(this::constraintViolation)
                                                   .toList()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest()
                             .body(toDtos(exception.getBindingResult()
                                                   .getFieldErrors()
                                                   .stream()
                                                   .sorted(Comparator.comparing(FieldError::getField))
                                                   .map(error -> new ValidationViolation("REQUEST_CONSTRAINT_VIOLATION",
                                                           "Request parameter must satisfy the declared constraint.",
                                                           "/" + error.getField()))
                                                   .toList()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleUnreadableRequestBody() {
        return ResponseEntity.badRequest()
                             .body(toDtos(List.of(new ValidationViolation("REQUEST_BODY_UNREADABLE",
                                     "Request body must be readable JSON.", "/"))));
    }

    @ExceptionHandler(TrailsException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleTrailsFailure(TrailsException exception) {
        int status = determineStatus(exception);
        ValidationViolation error = new ValidationViolation("TRAILS_ERROR", exception.userMessage(), "/");
        List<ValidationViolation> errors = List.of(error);
        List<ValidationErrorDTO> dtos = toDtos(errors);
        return ResponseEntity.status(status)
                             .body(dtos);
    }

    private static int determineStatus(TrailsException exception) {
        if (exception.errorClassification() == ErrorClassification.NOT_FOUND) {
            return HttpStatus.NOT_FOUND.value();
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    ResponseEntity<Void> handleAuthorizationDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                             .build();
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<List<ValidationErrorDTO>> handleUnexpectedFailure(Exception exception) {
        log.warn("REST resource failure error={}", exception.getClass()
                                                            .getSimpleName());
        return ResponseEntity.internalServerError()
                             .body(toDtos(List.of(new ValidationViolation("INTERNAL_SERVER_ERROR",
                                     "An unexpected error occurred.", "/"))));
    }

    private List<ValidationErrorDTO> toDtos(List<ValidationViolation> violations) {
        return violations.stream()
                         .map(violation -> new ValidationErrorDTO(violation.code(), violation.message(),
                                 violation.path()))
                         .toList();
    }

    private ValidationViolation constraintViolation(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath()
                                       .toString();
        String parameter = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
        return new ValidationViolation("REQUEST_CONSTRAINT_VIOLATION",
                "Request parameter must satisfy the declared constraint.", "/" + parameter);
    }

}
