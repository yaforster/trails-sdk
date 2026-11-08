package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionElementReferences;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CoordinateClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TypingDetailsDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.test.action.element.ClickAction;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ActionElementReferenceResolverTest {

    @Test
    void shouldResolveRepeatedPageLocatorsOnceWithinStage() {
        ElementDatabaseService elements = mock(ElementDatabaseService.class);
        ElementDatabaseService.ElementLocator reference = new ElementDatabaseService.ElementLocator(1L, 2L, "#target");
        when(elements.getElementIDByLocator(reference)).thenReturn(7L);
        Locator locator = new Locator(LocatorType.CSS, "#target");
        ClickAction first = ClickAction.builder()
                                       .locatorForElementToActOn(locator)
                                       .build();
        ClickAction second = ClickAction.builder()
                                        .locatorForElementToActOn(locator)
                                        .build();
        List<PersistedAction> actions = List.of(new PersistedAction(1L, 3L, 4L, first),
                new PersistedAction(2L, 3L, 5L, second));

        ActionElementReferences references = new ActionElementReferenceResolver(elements).forPage(1L, 2L, actions);

        assertThat(references.elementId(locator)).isEqualTo(7L);
        verify(elements).getElementIDByLocator(reference);
        verifyNoMoreInteractions(elements);
    }

    @Test
    void shouldResolveReferencedDefinitionElementsOnceAndIgnoreOtherVariants() {
        ElementDatabaseService elements = mock(ElementDatabaseService.class);
        Locator locator = new Locator(LocatorType.CSS, "#target");
        when(elements.getElementLocator(7L)).thenReturn(locator);
        List<ActionDefinitionDTO> definitions = List.of(
                new ActionDefinitionDTO().details(new ClickDetailsDTO().elementID(7L)),
                new ActionDefinitionDTO().details(new TypingDetailsDTO().elementID(7L)),
                new ActionDefinitionDTO().details(new CoordinateClickDetailsDTO()));

        ActionElementReferences references = new ActionElementReferenceResolver(elements).forDefinition(definitions);

        assertThat(references.elementLocator(7L)).isEqualTo(locator);
        verify(elements).getElementLocator(7L);
        verifyNoMoreInteractions(elements);
    }
}
