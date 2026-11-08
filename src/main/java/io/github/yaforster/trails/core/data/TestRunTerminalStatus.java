package io.github.yaforster.trails.core.data;

/** The transient terminal host outcome of a test execution. */
public enum TestRunTerminalStatus {

    /** The browser execution completed, regardless of individual action outcomes. */
    COMPLETED,

    /**
     * The host could not complete the browser execution and stored a safe failure result.
     */
    FAILED

}
