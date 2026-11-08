package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CoordinateClickDetailsDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.List;

class CoordinateClickDetailsRawDeserializer extends StdDeserializer<CoordinateClickDetailsDTO> {

    private static final ActionDetailsRawFieldReader.ErrorMetadata X_COORDINATE_ERRORS = new ActionDetailsRawFieldReader.ErrorMetadata(
            "TEST_PLAN_COORDINATE_CLICK_X_COORDINATE_REQUIRED", "X coordinate is required.",
            "TEST_PLAN_COORDINATE_CLICK_X_COORDINATE_INVALID",
            "X coordinate must be an integer between 0 and 2147483647.");
    private static final ActionDetailsRawFieldReader.ErrorMetadata Y_COORDINATE_ERRORS = new ActionDetailsRawFieldReader.ErrorMetadata(
            "TEST_PLAN_COORDINATE_CLICK_Y_COORDINATE_REQUIRED", "Y coordinate is required.",
            "TEST_PLAN_COORDINATE_CLICK_Y_COORDINATE_INVALID",
            "Y coordinate must be an integer between 0 and 2147483647.");
    private final ActionDetailsRawFieldReader fieldReader = new ActionDetailsRawFieldReader();

    CoordinateClickDetailsRawDeserializer() {
        super(CoordinateClickDetailsDTO.class);
    }

    private static List<ValidationViolation> violations(ActionDetailsRawFieldReader.FieldRead xCoordinate,
            ActionDetailsRawFieldReader.FieldRead yCoordinate) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (xCoordinate.violation() != null) {
            violations.add(xCoordinate.violation());
        }
        if (yCoordinate.violation() != null) {
            violations.add(yCoordinate.violation());
        }
        return violations;
    }

    @Override
    public CoordinateClickDetailsDTO deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {
        TokenStreamContext parsingContext = parser.streamReadContext();
        JsonNode details = context.readTree(parser);
        ActionDetailsRawFieldReader.FieldRead xCoordinate = fieldReader.readNonNegativeInt32(details, parsingContext,
                "xCoordinate", X_COORDINATE_ERRORS);
        ActionDetailsRawFieldReader.FieldRead yCoordinate = fieldReader.readNonNegativeInt32(details, parsingContext,
                "yCoordinate", Y_COORDINATE_ERRORS);
        List<ValidationViolation> violations = violations(xCoordinate, yCoordinate);
        if (!violations.isEmpty()) {
            throw new ActionDetailsValidationException(parser, "Coordinate click details validation failed.",
                    violations);
        }
        return new CoordinateClickDetailsDTO(ActionDetailsTypeDTO.COORDINATE_CLICK, xCoordinate.value(),
                yCoordinate.value());
    }

}
