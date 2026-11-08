package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.persisted.PersistedActionResult;

import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ActionResultQueryService {

    Optional<List<PersistedActionResult>> listActionResultChain(Long pathResultId);

    Map<Long, List<PersistedActionResult>> listActionResultChains(ActionResultChains actionResultChains);

    record ActionResultChains(Collection<Long> pathResultIds) {

        public ActionResultChains {
            pathResultIds = List.copyOf(pathResultIds);
        }

    }

}
