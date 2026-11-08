package io.github.yaforster.trails.core.print;

import io.github.yaforster.trails.core.test.Browser;

import java.util.List;

public record TestSetPrintContext(Long id, Long testRunId, Long applicationId, Long stageId, Browser browser,
        List<TestPathPrintContext> pathPrintContexts) {
    public TestSetPrintContext {
        pathPrintContexts = List.copyOf(pathPrintContexts);
    }
}
