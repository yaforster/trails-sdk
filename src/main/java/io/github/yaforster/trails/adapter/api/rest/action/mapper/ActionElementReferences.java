package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.core.MappingException;
import io.github.yaforster.trails.core.test.action.element.Locator;
import java.util.Map;

/** Immutable element identities supplied to action mapping after transport-boundary resolution. */
public record ActionElementReferences(Map<String, Long> idsByLocator, Map<Long, Locator> locatorsById) {

    public ActionElementReferences {
        idsByLocator = Map.copyOf(idsByLocator);
        locatorsById = Map.copyOf(locatorsById);
    }

    public Long elementId(Locator locator) {
        Long id = idsByLocator.get(locator.locatorString());
        if (id == null) {
            throw new MappingException("No element ID was resolved for locator " + locator.locatorString());
        }
        return id;
    }

    public Locator elementLocator(Long id) {
        Locator locator = locatorsById.get(id);
        if (locator == null) {
            throw new MappingException("No locator was resolved for element ID " + id);
        }
        return locator;
    }

}
