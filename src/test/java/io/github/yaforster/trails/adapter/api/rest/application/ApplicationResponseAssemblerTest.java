package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationResponseAssemblerTest {
    @Test
    void shouldKeepRetirementScopeAcrossPages_whenInventoryAndPermissionsAreSupplied() {
        PagedResult<PersistedApplication> page = new PagedResult<>(List.of(), 1, 10, 25);

        Map<String, LinkDTO> links = new ApplicationResponseAssembler().applications(page, Set.of(), true, false)
                                                                       .getLinks();

        assertThat(List.of("self", "first", "last", "prev", "next")
                       .stream()
                       .map(key -> links.get(key)
                                        .getHref())
                       .toList()).containsExactly("/api/applications?page=1&size=10&includeRetired=true",
                               "/api/applications?page=0&size=10&includeRetired=true",
                               "/api/applications?page=2&size=10&includeRetired=true",
                               "/api/applications?page=0&size=10&includeRetired=true",
                               "/api/applications?page=2&size=10&includeRetired=true");
    }

    @Test
    void shouldPublishChildLink_onlyWhenSuppliedInventoryContainsApplication() {
        PersistedApplication application = new PersistedApplication(7L, "app", true);

        assertThat(new ApplicationResponseAssembler()
                                                     .applications(PagedResult.singlePage(List.of(application)),
                                                             Set.of(7L), true, false)
                                                     .getItems()
                                                     .getFirst()
                                                     .getLinks()).containsKey("stages");
    }
}
