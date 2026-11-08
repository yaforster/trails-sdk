package io.github.yaforster.trails.core.deletion;

public record DeletionFailure(Long id, String message, ErrorDetails error) implements DatabaseDeletionResult {

    private static final String UNEXPECTED_DELETION_FAILURE_MESSAGE = "The requested item could not be deleted.";

    public DeletionFailure(Long id, Throwable throwable) {
        this(id, UNEXPECTED_DELETION_FAILURE_MESSAGE, ErrorDetails.fromException(throwable));
    }
}
