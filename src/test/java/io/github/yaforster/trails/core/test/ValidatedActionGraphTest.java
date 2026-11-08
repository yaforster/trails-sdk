package io.github.yaforster.trails.core.test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.*;

class ValidatedActionGraphTest {
    static List<Map<Long, List<Long>>> invalidGraphs() {
        return List.of(Map.of(), Map.of(1L, List.of(2L)), Map.of(1L, List.of(), 2L, List.of()),
                Map.of(1L, List.of(2L), 2L, List.of(1L)), Map.of(1L, List.of(2L, 2L), 2L, List.of()),
                Map.of(1L, List.of(), 2L, List.of(3L), 3L, List.of(2L)));
    }

    @ParameterizedTest
    @MethodSource("invalidGraphs")
    void shouldRejectMalformedGraphs_whenTheyCannotDefineExecution(Map<Long, List<Long>> graph) {
        assertThatThrownBy(() -> ValidatedActionGraph.validate(graph, ActionGraphLimits.DEFAULTS)).isInstanceOf(
                IllegalArgumentException.class);
    }

    @Test
    void shouldCountSharedActionsPerPath_whenGraphReconverges() {
        Map<Long, List<Long>> diamond = Map.of(1L, List.of(2L, 3L), 2L, List.of(4L), 3L, List.of(4L), 4L, List.of());
        assertThatThrownBy(() -> ValidatedActionGraph.validate(diamond,
                new ActionGraphLimits(4, 2, 5))).isInstanceOf(IllegalArgumentException.class)
                                                .hasMessageContaining("maximum action occurrences: 5");
    }

    @Test
    void shouldAcceptInclusiveLimits_whenDiamondFitsExactly() {
        Map<Long, List<Long>> diamond = Map.of(1L, List.of(2L, 3L), 2L, List.of(4L), 3L, List.of(4L), 4L, List.of());
        assertThat(ValidatedActionGraph.validate(diamond, new ActionGraphLimits(4, 2, 6))
                                       .executionOrder()).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void shouldRejectExponentialExpansion_withoutEnumeratingPaths() {
        Map<Long, List<Long>> graph = new LinkedHashMap<>();
        for (long id = 1; id < 300; id += 3) {
            graph.put(id, List.of(id + 1, id + 2));
            graph.put(id + 1, List.of(id + 3));
            graph.put(id + 2, List.of(id + 3));
        }
        graph.put(301L, List.of());
        assertThatThrownBy(
                () -> ValidatedActionGraph.validate(graph, ActionGraphLimits.DEFAULTS)).isInstanceOf(
                        IllegalArgumentException.class)
                                                                                       .hasMessageContaining(
                                                                                               "maximum paths");
    }
}
