package io.github.yaforster.trails.core;

import lombok.experimental.StandardException;

@StandardException
public abstract class TrailsException extends RuntimeException {

    public ErrorClassification errorClassification() {
        return ErrorClassification.INTERNAL_ERROR;
    }

    public String userMessage() {
        return switch (errorClassification()) {
            case INTERNAL_ERROR -> "An unexpected error occurred.";
            case NOT_FOUND -> "The requested resource was not found.";
        };
    }

}
