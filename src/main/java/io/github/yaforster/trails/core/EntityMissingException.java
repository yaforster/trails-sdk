package io.github.yaforster.trails.core;

import lombok.experimental.StandardException;

@StandardException
public class EntityMissingException extends TrailsException {

    @Override
    public ErrorClassification errorClassification() {
        return ErrorClassification.NOT_FOUND;
    }

}
