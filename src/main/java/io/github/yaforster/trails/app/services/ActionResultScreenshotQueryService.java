package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ActionResultScreenshotQueryService {
    /** Returns screenshot-bearing result identifiers within one path without loading binary content. */
    Set<Long> findActionResultIdsInPath(Long pathResultId);

    Optional<PersistedArtifactFile> findInPath(PathActionScreenshot request);

    Map<Long, PersistedArtifactFile> findAll(ActionScreenshots request);

    record PathActionScreenshot(Long pathResultId, Long actionResultId) {
    }

    record ActionScreenshots(Collection<Long> actionResultIds) {

        public ActionScreenshots {
            actionResultIds = List.copyOf(actionResultIds);
        }

    }

}
