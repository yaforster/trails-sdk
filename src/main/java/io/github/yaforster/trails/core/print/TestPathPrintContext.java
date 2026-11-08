package io.github.yaforster.trails.core.print;

import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.test.Browser;

import java.util.List;

public record TestPathPrintContext(Long pathResultId, Long testSetResultId, String pathLabel, Browser browser,
        List<PersistedActionResult> actionResults) {
    public TestPathPrintContext {
        actionResults = List.copyOf(actionResults);
    }
}
