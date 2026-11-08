package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DeploymentControllerTest extends TrailsTest {

    @Test
    void shouldStoreAValidatedDeploymentAndReturnItsLocation() {
        DeploymentDatabaseService deployments = mock(DeploymentDatabaseService.class);
        Instant deployedAt = Instant.parse("2026-10-03T12:00:00Z");
        when(deployments.storeDeployment(new DeploymentDatabaseService.StageDeployment(12L, 23L,
                new DeploymentDefinition("1.2.3", deployedAt)))).thenReturn(
                        Optional.of(new PersistedDeployment(34L, 12L, 23L, "1.2.3", deployedAt)));
        DeploymentController controller = controller(deployments);

        ResponseEntity<DeploymentDTO> response = controller.reportDeployment(12L, 23L,
                new DeploymentDefinitionDTO(" 1.2.3 ", OffsetDateTime.ofInstant(deployedAt, java.time.ZoneOffset.UTC)));

        assertThat(response).satisfies(result -> {
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(result.getHeaders()
                             .getLocation()).isEqualTo(URI.create("/api/applications/12/stages/23/deployments/34"));
            assertThat(result.getBody()
                             .getLinks()
                             .get("collection")
                             .getHref()).isEqualTo("/api/applications/12/stages/23/deployments?page=0&size=20");
        });
    }

    private DeploymentController controller(DeploymentDatabaseService deployments) {
        TrailsAuthorization authorization = mock(TrailsAuthorization.class);
        when(authorization.canReportDeployments()).thenReturn(true);
        return new DeploymentController(deployments, authorization, new DeploymentRequestValidator(),
                new DeploymentResponseAssembler());
    }

}
