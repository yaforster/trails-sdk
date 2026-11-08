package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.core.ValidationViolation;
import java.util.List;
import java.util.Objects;

/** Signals a client-visible validation failure in a test execution request. */
public final class TestExecutionRequestValidationException extends RuntimeException {

    private final List<ValidationViolation> violations;

    public TestExecutionRequestValidationException(List<ValidationViolation> violations) {
        this.violations = List.copyOf(Objects.requireNonNull(violations, "Validation violations are required"));
    }

    public List<ValidationViolation> violations() {
        return violations;
    }

}
