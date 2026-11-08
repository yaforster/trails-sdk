package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import io.github.yaforster.trails.core.test.action.element.ClickAction;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class ExecutionSnapshotTest {
    @Test
    void shouldRepresentUnavailableArtifactContent_asEmptyBytes() {
        PersistedArtifactFile artifact = new PersistedArtifactFile(1L, "missing.png", "image/png", null);
        assertThat(artifact.content()).isEmpty();
    }
    @Test
    void shouldKeepFileBytesStable_whenCallerMutatesInputAndAccessedContent() {
        byte[] bytes = {1, 2};
        FileData downloaded = new FileData(bytes, "file");
        PersistedArtifactFile stored = new PersistedArtifactFile(1L, "file", "application/octet-stream", bytes);
        bytes[0] = 9;
        downloaded.content()[1] = 9;
        stored.content()[1] = 9;
        assertThat(List.of(downloaded.content(), stored.content())).allSatisfy(
                content -> assertThat(content).containsExactly(1, 2));
    }

    @Test
    void shouldRetainExecutionActionsAndEdges_whenAdapterMutatesSourceLists() {
        List<Long> edges = new ArrayList<>(List.of(2L));
        Action action = ClickAction.builder()
                                   .actionID(1L)
                                   .nextActions(edges)
                                   .build();
        List<Action> actions = new ArrayList<>(List.of(action));
        TestExecutionData snapshot = new TestExecutionData(List.of(), actions, 1L, 2L, 3L, "Plan");
        actions.clear();
        edges.clear();
        assertThat(snapshot.actions()).singleElement()
                                      .satisfies(retained -> assertThat(retained.getNextActions()).containsExactly(2L));
    }

    @Test
    void shouldProtectExecutionTimestamp_whenCallerMutatesIt() {
        java.sql.Timestamp timestamp = java.sql.Timestamp.valueOf("2026-10-31 12:00:00.123456789");
        java.time.Instant expected = timestamp.toInstant();
        TestCaseResult result = new TestCaseResult(timestamp, Browser.CHROME, List.of());
        timestamp.setTime(0);
        result.timestamp()
              .setTime(1);
        assertThat(result.timestamp()
                         .toInstant()).isEqualTo(expected);
    }
}
