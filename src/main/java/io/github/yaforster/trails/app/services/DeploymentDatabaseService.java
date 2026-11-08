package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;

import java.util.Optional;

public interface DeploymentDatabaseService {

    record StageDeployment(Long applicationId, Long stageId, DeploymentDefinition definition) {
    }

    record DeploymentDetails(Long applicationId, Long stageId, Long deploymentId) {
    }

    record StageDeploymentPage(Long applicationId, Long stageId, int page, int size) {
    }

    Optional<PersistedDeployment> storeDeployment(StageDeployment stageDeployment);

    Optional<PersistedDeployment> getDeployment(DeploymentDetails deploymentDetails);

    Optional<PagedResult<PersistedDeployment>> listDeployments(StageDeploymentPage deploymentPage);

}
