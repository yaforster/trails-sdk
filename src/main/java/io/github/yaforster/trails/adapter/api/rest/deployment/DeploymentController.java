package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedDeploymentDTO;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import java.util.Objects;
import org.openapitools.api.DeploymentApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the contract-defined deployment resource through the validated persistence composition.
 */
@RestController
public class DeploymentController implements DeploymentApi {

    private final DeploymentDatabaseService deployments;
    private final TrailsAuthorization authorization;
    private final DeploymentRequestValidator requests;
    private final DeploymentResponseAssembler responses;

    DeploymentController(DeploymentDatabaseService deployments, TrailsAuthorization authorization,
            DeploymentRequestValidator requests, DeploymentResponseAssembler responses) {
        this.deployments = Objects.requireNonNull(deployments, "Deployment database service is required");
        this.authorization = Objects.requireNonNull(authorization, "Trails authorization is required");
        this.requests = Objects.requireNonNull(requests, "Deployment request validator is required");
        this.responses = Objects.requireNonNull(responses, "Deployment response assembler is required");
    }

    @Override
    public ResponseEntity<DeploymentDTO> getDeployment(Long applicationId, Long stageId, Long deploymentId) {
        requests.validateDeploymentReference(applicationId, stageId, deploymentId);
        return deployments.getDeployment(
                new DeploymentDatabaseService.DeploymentDetails(applicationId, stageId, deploymentId))
                          .map(responses::deployment)
                          .map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound()
                                                         .build());
    }

    @Override
    public ResponseEntity<PagedDeploymentDTO> listDeployments(Long applicationId, Long stageId, Integer page,
            Integer size) {
        requests.validatePage(applicationId, stageId, page, size);
        return deployments.listDeployments(
                new DeploymentDatabaseService.StageDeploymentPage(applicationId, stageId, page, size))
                          .map(deployments -> responses.deployments(applicationId, stageId, deployments,
                                  authorization.canReportDeployments()))
                          .map(ResponseEntity::ok)
                          .orElseGet(() -> ResponseEntity.notFound()
                                                         .build());
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canReportDeployments(authentication)")
    public ResponseEntity<DeploymentDTO> reportDeployment(Long applicationId, Long stageId,
            DeploymentDefinitionDTO request) {
        requests.validateReport(applicationId, stageId, request);
        return deployments.storeDeployment(
                new DeploymentDatabaseService.StageDeployment(applicationId, stageId, responses.definition(request)))
                          .map(this::created)
                          .orElseGet(() -> ResponseEntity.notFound()
                                                         .build());
    }

    private ResponseEntity<DeploymentDTO> created(PersistedDeployment deployment) {
        return ResponseEntity.created(responses.location(deployment))
                             .body(responses.deployment(deployment));
    }

}
