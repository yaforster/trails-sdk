package io.github.yaforster.trails.core.deletion;

public sealed interface DatabaseDeletionResult permits DeletionSuccess, DeletionNotFound, DeletionFailure {

}
