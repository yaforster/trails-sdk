package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.core.ValidationViolation;
import lombok.Getter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;

import java.util.List;

@Getter
public class ActionDetailsValidationException extends JacksonException {

    private final List<ValidationViolation> validationViolations;

    ActionDetailsValidationException(JsonParser parser, String message,
            List<ValidationViolation> validationViolations) {
        super(parser, message);
        this.validationViolations = List.copyOf(validationViolations);
    }

}
