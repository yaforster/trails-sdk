package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DeploymentResponseAssemblerTest {

    @Test
    void shouldOfferCreateLinkOnlyForSuppliedReportingPermission() {
        PagedResult<PersistedDeployment> page = PagedResult.singlePage(List.of());
        DeploymentResponseAssembler assembler = new DeploymentResponseAssembler();

        boolean denied = assembler.deployments(1L, 2L, page, false)
                                  .getLinks()
                                  .containsKey("create");
        boolean allowed = assembler.deployments(1L, 2L, page, true)
                                   .getLinks()
                                   .containsKey("create");

        assertThat(List.of(denied, allowed)).containsExactly(false, true);
    }
}
