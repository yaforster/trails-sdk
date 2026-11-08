package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.ElementDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ElementTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LocatorTypeDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElementRequestValidatorTest {

    @Test
    void shouldRejectEveryMissingCreateValue() {
        ElementRequestValidator validator = new ElementRequestValidator(mock(ElementDatabaseService.class));

        assertThatThrownBy(() -> validator.validateCreate(null, null,
                null)).isInstanceOf(ApiRequestValidationException.class)
                      .extracting(exception -> ((ApiRequestValidationException) exception).violations())
                      .isEqualTo(List.of(
                              new ValidationViolation("ELEMENT_APPLICATION_ID_NULL", "Application id must not be null.",
                                      "/applicationId"),
                              new ValidationViolation("ELEMENT_STAGE_ID_NULL", "Stage id must not be null.",
                                      "/stageId"),
                              new ValidationViolation("ELEMENT_TYPE_NULL", "Element type must not be null.", "/type"),
                              new ValidationViolation("ELEMENT_LABEL_EMPTY", "Element label must not be empty.",
                                      "/label"),
                              new ValidationViolation("ELEMENT_LOCATOR_STRING_EMPTY",
                                      "Element locator string must not be empty.", "/locatorString"),
                              new ValidationViolation("ELEMENT_LOCATOR_TYPE_NULL",
                                      "Element locator type must not be null.", "/locatorType")));
    }

    @Test
    void shouldRejectDuplicateLocatorWhenCreatingElement() {
        ElementDatabaseService elements = mock(ElementDatabaseService.class);
        when(elements.existsByLocatorString(
                new ElementDatabaseService.ElementLocatorText(1L, 2L, "#login"))).thenReturn(true);
        ElementRequestValidator validator = new ElementRequestValidator(elements);

        assertThatThrownBy(() -> validator.validateCreate(1L, 2L,
                definition())).isInstanceOf(ApiRequestValidationException.class)
                              .extracting(exception -> ((ApiRequestValidationException) exception).violations())
                              .isEqualTo(List.of(new ValidationViolation("ELEMENT_LOCATOR_STRING_ALREADY_EXISTS",
                                      "Element locator string must be unique within the stage.", "/locatorString")));
    }

    @Test
    void shouldRejectPromotionToSourceStage() {
        ElementRequestValidator validator = new ElementRequestValidator(mock(ElementDatabaseService.class));

        assertThatThrownBy(() -> validator.validatePromotion(1L, 2L, 3L,
                2L)).isInstanceOf(ApiRequestValidationException.class)
                    .extracting(exception -> ((ApiRequestValidationException) exception).violations())
                    .isEqualTo(List.of(new ValidationViolation("ELEMENT_PROMOTE_TARGET_STAGE_EQUALS_SOURCE_STAGE",
                            "Target stage must be different from source stage.", "/targetStageId")));
    }

    @Test
    void shouldAcceptCompleteCreateRequestWithUniqueValues() {
        ElementRequestValidator validator = new ElementRequestValidator(mock(ElementDatabaseService.class));

        assertThatCode(() -> validator.validateCreate(1L, 2L, definition())).doesNotThrowAnyException();
    }

    private ElementDefinitionDTO definition() {
        return new ElementDefinitionDTO().type(ElementTypeDTO.BUTTON)
                                         .label("Login")
                                         .locatorString("#login")
                                         .locatorType(LocatorTypeDTO.CSS);
    }

}
