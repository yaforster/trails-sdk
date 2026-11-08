package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElementResponseAssemblerTest {
    @Test
    void shouldIncludeMutationLinksOnlyForSuppliedPermission() {
        PersistedElement element = new PersistedElement(8L, "element", LocatorType.CSS, 7L, 9L, "#target",
                ElementType.BUTTON, false);
        ElementResponseAssembler assembler = new ElementResponseAssembler();

        boolean denied = assembler.element(7L, 9L, element, Optional.empty(), false, false)
                                  .getLinks()
                                  .containsKey("delete");
        boolean allowed = assembler.element(7L, 9L, element, Optional.empty(), false, true)
                                   .getLinks()
                                   .containsKey("delete");

        assertThat(List.of(denied, allowed)).containsExactly(false, true);
    }

    @Test
    void shouldKeepRetirementScope_whenLinkingToRetiredElement() {
        PersistedElement element = new PersistedElement(8L, "element", LocatorType.CSS, 7L, 9L, "#target",
                ElementType.BUTTON, true);

        String selfUri = new ElementResponseAssembler().element(7L, 9L, element, Optional.empty(), true, false)
                                                       .getLinks()
                                                       .get("self")
                                                       .getHref();

        assertThat(selfUri).isEqualTo("/api/applications/7/stages/9/elements/8?includeRetired=true");
    }
}
