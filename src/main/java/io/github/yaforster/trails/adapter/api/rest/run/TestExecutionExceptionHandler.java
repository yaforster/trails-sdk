package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.model.ValidationErrorDTO;
import io.github.yaforster.trails.core.ErrorClassification;
import io.github.yaforster.trails.core.TrailsException;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Maps execution request failures to the contract's structured, safe error response. */
@RestControllerAdvice(assignableTypes = TestExecutionController.class)
public final class TestExecutionExceptionHandler {

    @ExceptionHandler(TestExecutionRequestValidationException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleRequestValidation(
            TestExecutionRequestValidationException exception) {
        return ResponseEntity.badRequest()
                             .body(toDtos(exception.violations()));
    }

    @ExceptionHandler(TrailsException.class)
    ResponseEntity<List<ValidationErrorDTO>> handleTrailsFailure(TrailsException exception) {
        int status;
        if (exception.errorClassification() == ErrorClassification.NOT_FOUND) {
            status = 404;
        } else {
            status = 500;
        }
        return ResponseEntity.status(status)
                             .body(toDtos(
                                     List.of(new ValidationViolation("TRAILS_ERROR", exception.userMessage(), "/"))));
    }

    private List<ValidationErrorDTO> toDtos(List<ValidationViolation> violations) {
        return violations.stream()
                         .map(this::toDto)
                         .toList();
    }

    private ValidationErrorDTO toDto(ValidationViolation violation) {
        return new ValidationErrorDTO(violation.code(), violation.message(), violation.path());
    }

}
