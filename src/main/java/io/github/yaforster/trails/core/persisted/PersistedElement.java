package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.test.action.element.LocatorType;

public record PersistedElement(Long id, String label, LocatorType locatorType, Long applicationId, Long stageId,
        String locator, ElementType type, boolean retired) {

    public PersistedElement(Long id, String label, LocatorType locatorType, Long applicationId, Long stageId,
            String locator, ElementType type) {
        this(id, label, locatorType, applicationId, stageId, locator, type, false);
    }
}
