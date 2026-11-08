package io.github.yaforster.trails.core.definition;

import io.github.yaforster.trails.core.data.TestDataEntry;

import java.util.List;

public record TestDataSetDefinition(String label, List<TestDataEntry> values) {
    public TestDataSetDefinition {
        values = List.copyOf(values);
    }
}
