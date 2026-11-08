package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedAction;

import java.util.Optional;

@FunctionalInterface
public interface ActionQueryService {

    Optional<PagedResult<PersistedAction>> listActions(ActionPage actionPage);

    record ActionPage(Long applicationId, Long stageId, Long testPlanId, int page, int size) {
    }

}
