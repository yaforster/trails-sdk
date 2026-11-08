package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StageResponseAssemblerTest {
    @Test
    void shouldPreserveRetirementScopeAndChildInventory_whenBuildingStageLinks() {
        PersistedStage stage = new PersistedStage(8L, 7L, "stage", "https://example.test", true);

        String href = new StageResponseAssembler().stages(7L, PagedResult.singlePage(List.of(stage)), Set.of(8L), true,
                false)
                                                  .getItems()
                                                  .getFirst()
                                                  .getLinks()
                                                  .get("elements")
                                                  .getHref();

        assertThat(href).isEqualTo("/api/applications/7/stages/8/elements?page=0&size=20&includeRetired=true");
    }

    @Test
    void shouldOmitMutationLink_whenPermissionIsAbsent() {
        PersistedStage stage = new PersistedStage(8L, 7L, "stage", "https://example.test", true);

        assertThat(new StageResponseAssembler().stage(7L, stage, false, true, false)
                                               .getLinks()).doesNotContainKey("restore");
    }
}
