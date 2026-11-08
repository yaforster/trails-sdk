package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;

import java.util.Optional;

public interface ArtifactQueryService {

    PagedResult<PersistedArtifact> listArtifacts(PathArtifactPage pathArtifactPage);

    Optional<PersistedArtifactFile> findArtifactInPath(PathArtifact pathArtifact);

    record PathArtifactPage(Long testPathResultId, int page, int size) {
    }

    record PathArtifact(Long pathResultId, Long artifactId) {
    }

}
