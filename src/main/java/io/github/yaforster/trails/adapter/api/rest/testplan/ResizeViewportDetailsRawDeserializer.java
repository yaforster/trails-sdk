package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ResizeViewportDetailsDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TokenStreamContext;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.util.ArrayList;
import java.util.List;

class ResizeViewportDetailsRawDeserializer extends StdDeserializer<ResizeViewportDetailsDTO> {

    private static final ActionDetailsRawFieldReader.ErrorMetadata VIEWPORT_WIDTH_ERRORS = new ActionDetailsRawFieldReader.ErrorMetadata(
            "TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_WIDTH_REQUIRED", "Viewport width is required.",
            "TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_WIDTH_INVALID",
            "Viewport width must be an integer between 1 and 2147483647.");
    private static final ActionDetailsRawFieldReader.ErrorMetadata VIEWPORT_HEIGHT_ERRORS = new ActionDetailsRawFieldReader.ErrorMetadata(
            "TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_HEIGHT_REQUIRED", "Viewport height is required.",
            "TEST_PLAN_RESIZE_VIEWPORT_VIEWPORT_HEIGHT_INVALID",
            "Viewport height must be an integer between 1 and 2147483647.");
    private final ActionDetailsRawFieldReader fieldReader = new ActionDetailsRawFieldReader();

    ResizeViewportDetailsRawDeserializer() {
        super(ResizeViewportDetailsDTO.class);
    }

    @Override
    public ResizeViewportDetailsDTO deserialize(JsonParser parser, DeserializationContext context)
            throws JacksonException {
        TokenStreamContext parsingContext = parser.streamReadContext();
        JsonNode details = context.readTree(parser);
        ActionDetailsRawFieldReader.FieldRead viewportWidth = fieldReader.readPositiveInt32(details, parsingContext,
                "viewportWidth", VIEWPORT_WIDTH_ERRORS);
        ActionDetailsRawFieldReader.FieldRead viewportHeight = fieldReader.readPositiveInt32(details, parsingContext,
                "viewportHeight", VIEWPORT_HEIGHT_ERRORS);
        List<ValidationViolation> violations = violations(viewportWidth, viewportHeight);
        if (!violations.isEmpty()) {
            throw new ActionDetailsValidationException(parser, "Resize viewport details validation failed.",
                    violations);
        }
        return new ResizeViewportDetailsDTO(ActionDetailsTypeDTO.RESIZE_VIEWPORT, viewportWidth.value(),
                viewportHeight.value());
    }

    private static List<ValidationViolation> violations(ActionDetailsRawFieldReader.FieldRead viewportWidth,
            ActionDetailsRawFieldReader.FieldRead viewportHeight) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (viewportWidth.violation() != null) {
            violations.add(viewportWidth.violation());
        }
        if (viewportHeight.violation() != null) {
            violations.add(viewportHeight.violation());
        }
        return violations;
    }

}
