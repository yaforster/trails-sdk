package io.github.yaforster.trails.core.definition;

import io.github.yaforster.trails.core.data.Element;

import java.util.List;

public record StageDefinition(String label, String url, List<Element> availableElements) {
    public StageDefinition {
        availableElements = List.copyOf(availableElements);
    }

}
