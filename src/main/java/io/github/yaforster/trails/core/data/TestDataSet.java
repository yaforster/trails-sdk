package io.github.yaforster.trails.core.data;

import java.time.Instant;
import java.util.List;

public record TestDataSet(Long id, String label, Instant creationDate, List<TestDataEntry> values, boolean retired) {
}
