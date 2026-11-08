package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.ArtifactType;

public record PersistedArtifact(Long id, Long actionResultId, ArtifactType type, String filename, String contentType,
        long size, String storageKey) {

}
