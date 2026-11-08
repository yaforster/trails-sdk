package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionElementReferences;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CheckExistenceDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ClickDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ElementValueCheckDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.SelectionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TextCheckDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TypingDetailsDTO;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.action.element.Locator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/** Resolves action element references at the controller boundary, outside response and request mappers. */
public final class ActionElementReferenceResolver {

    private final ElementDatabaseService elements;

    public ActionElementReferenceResolver(ElementDatabaseService elements) {
        this.elements = Objects.requireNonNull(elements, "Element database service is required");
    }

    public ActionElementReferences forPage(Long applicationId, Long stageId, List<PersistedAction> actions) {
        Map<String, Long> ids = new LinkedHashMap<>();
        for (PersistedAction persistedAction : actions) {
            if (persistedAction.action() instanceof WebpageAction webpageAction) {
                String locator = webpageAction.getLocatorForElementToActOn()
                                              .locatorString();
                ids.computeIfAbsent(locator, key -> elements.getElementIDByLocator(
                        new ElementDatabaseService.ElementLocator(applicationId, stageId, key)));
            }
        }
        return new ActionElementReferences(ids, Map.of());
    }

    public ActionElementReferences forDefinition(List<ActionDefinitionDTO> definitions) {
        Map<Long, Locator> locators = new LinkedHashMap<>();
        definitions.stream()
                   .map(ActionDefinitionDTO::getDetails)
                   .flatMap(this::elementIds)
                   .forEach(id -> locators.computeIfAbsent(id, elements::getElementLocator));
        return new ActionElementReferences(Map.of(), locators);
    }

    private Stream<Long> elementIds(ActionDetailsDTO details) {
        return switch (details) {
            case CheckExistenceDetailsDTO check -> Stream.of(check.getElementID());
            case ClickDetailsDTO click -> Stream.of(click.getElementID());
            case ElementValueCheckDetailsDTO check -> Stream.of(check.getElementID());
            case SelectionDetailsDTO selection -> Stream.of(selection.getElementID());
            case TextCheckDetailsDTO check -> Stream.of(check.getElementID());
            case TypingDetailsDTO typing -> Stream.of(typing.getElementID());
            default -> Stream.empty();
        };
    }
}
