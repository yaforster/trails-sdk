package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import java.util.Objects;

/**
 * Transient terminal outcome used to publish events and dispatch notifications after persistence.
 *
 * @param result
 *            persisted test-run result
 * @param status
 *            terminal execution status
 */
public record TestRunTerminalOutcome(PersistedTestRunResult result, TestRunTerminalStatus status) {

    public TestRunTerminalOutcome {
        Objects.requireNonNull(result, "Persisted test run result is required");
        Objects.requireNonNull(status, "Test run terminal status is required");
    }

}
