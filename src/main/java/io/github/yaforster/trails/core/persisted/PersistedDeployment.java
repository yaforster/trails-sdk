package io.github.yaforster.trails.core.persisted;

import java.time.Instant;

public record PersistedDeployment(Long id, Long applicationId, Long stageId, String version, Instant deployedAt) {
}
