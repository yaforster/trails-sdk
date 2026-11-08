package io.github.yaforster.trails.core.test;

/** Identifies a graph invariant without coupling core validation to HTTP error codes. */
public final class ActionGraphValidationException extends IllegalArgumentException {
    public enum Reason {
        ACTION_COUNT,
        IDENTIFIER,
        SUCCESSORS,
        UNKNOWN_SUCCESSOR,
        STARTING_ACTION,
        CYCLE,
        PATH_COUNT,
        ACTION_OCCURRENCES
    }

    private final Reason reason;

    public ActionGraphValidationException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason reason() {
        return reason;
    }
}
