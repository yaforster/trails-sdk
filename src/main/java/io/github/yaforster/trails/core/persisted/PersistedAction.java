package io.github.yaforster.trails.core.persisted;

import io.github.yaforster.trails.core.test.Action;

public record PersistedAction(Long id, Long testPlanId, Long referenceId, Action action) {

}
