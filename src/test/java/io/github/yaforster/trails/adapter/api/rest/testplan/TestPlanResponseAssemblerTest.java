package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestPlanResponseAssemblerTest {

    @Test
    void shouldOfferExecutionWithoutMutation_whenOnlyExecutionIsPermitted() {
        PersistedTestPlan plan = new PersistedTestPlan(3L, 1L, 2L, "plan");
        TestPlanResponseAssembler assembler = new TestPlanResponseAssembler();
        TestPlanResponseAssembler.TestPlanPermissions permissions = new TestPlanResponseAssembler.TestPlanPermissions(
                false, true);

        Map<String, LinkDTO> links = assembler.persistedTestPlan(1L, 2L, plan, false, false, permissions)
                                              .getLinks();

        assertThat(links).containsKey("execute")
                         .doesNotContainKeys("delete", "promote");
    }

    @Test
    void shouldOfferMutationWithoutExecution_whenOnlyManagementIsPermitted() {
        PersistedTestPlan plan = new PersistedTestPlan(3L, 1L, 2L, "plan");
        TestPlanResponseAssembler assembler = new TestPlanResponseAssembler();
        TestPlanResponseAssembler.TestPlanPermissions permissions = new TestPlanResponseAssembler.TestPlanPermissions(
                true, false);

        Map<String, LinkDTO> links = assembler.persistedTestPlan(1L, 2L, plan, false, false, permissions)
                                              .getLinks();

        assertThat(links).containsKey("delete")
                         .doesNotContainKey("execute");
    }
}
