package io.github.yaforster.trails.core.test;

import java.sql.Timestamp;
import java.util.List;

public record TestCaseResult(Timestamp timestamp, Browser testedInBrowser, List<TestPathResult> results) {
    public TestCaseResult {
        timestamp = Timestamp.from(timestamp.toInstant());
        results = List.copyOf(results);
    }

    @Override
    public Timestamp timestamp() {
        return Timestamp.from(timestamp.toInstant());
    }

}
