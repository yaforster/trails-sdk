package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.test.action.element.LocatorType;

public record Element(Long id, ElementType type, String label, String base64Screenshot, String locatorString,
        LocatorType locatorType) {

}
