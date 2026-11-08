package io.github.yaforster.trails.core.definition;

import java.time.Instant;

public record DeploymentDefinition(String version, Instant deployedAt) {
}
