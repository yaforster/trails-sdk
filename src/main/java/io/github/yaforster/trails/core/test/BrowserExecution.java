package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.action.document.DocumentExecution;

public record BrowserExecution(BrowserSession browser, DocumentExecution documents) {
}
