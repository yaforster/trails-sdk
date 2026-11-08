package io.github.yaforster.trails.core.definition;

import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.test.action.element.LocatorType;

public record ElementDefinition(ElementType type, String label, String locatorString, LocatorType locatorType) {

}
