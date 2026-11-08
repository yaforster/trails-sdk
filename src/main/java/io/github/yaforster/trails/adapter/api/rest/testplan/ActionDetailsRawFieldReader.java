package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.core.ValidationViolation;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.databind.JsonNode;

final class ActionDetailsRawFieldReader {

    FieldRead readNonNegativeInt32(JsonNode details, TokenStreamContext parsingContext, String field,
            ErrorMetadata errors) {
        return readInt32(details, parsingContext, field, 0, errors);
    }

    FieldRead readPositiveInt32(JsonNode details, TokenStreamContext parsingContext, String field,
            ErrorMetadata errors) {
        return readInt32(details, parsingContext, field, 1, errors);
    }

    private FieldRead readInt32(JsonNode details, TokenStreamContext parsingContext, String field, int minimum,
            ErrorMetadata errors) {
        JsonNode value = details.get(field);
        if (value == null || value.isNull()) {
            return new FieldRead(null, new ValidationViolation(errors.requiredCode(), errors.requiredMessage(),
                    path(parsingContext, field)));
        }
        if (!value.isIntegralNumber() || !value.canConvertToInt() || value.intValue() < minimum) {
            return new FieldRead(null, new ValidationViolation(errors.invalidCode(), errors.invalidMessage(),
                    path(parsingContext, field)));
        }
        return new FieldRead(value.intValue(), null);
    }

    private String path(TokenStreamContext parsingContext, String field) {
        for (TokenStreamContext context = parsingContext; context != null; context = context.getParent()) {
            TokenStreamContext parent = context.getParent();
            if (context.inArray() && parent != null && "testSteps".equals(parent.currentName())) {
                return "/testSteps/" + context.getCurrentIndex() + "/details/" + field;
            }
        }
        throw new IllegalStateException("Action details must be nested in testSteps.");
    }

    record ErrorMetadata(String requiredCode, String requiredMessage, String invalidCode, String invalidMessage) {
    }

    record FieldRead(Integer value, ValidationViolation violation) {
    }

}
