package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.TestStep;
import io.github.yaforster.trails.core.test.action.element.ClickAction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.assertj.core.api.Assertions.assertThat;

class TestStepArrangerTest {
    @Test
    void shouldRejectDuplicateIdentifiers_whenAdapterReturnsAmbiguousActions() {
        assertThatThrownBy(() -> new TestStepArranger().testStepChain(
                List.of(action(1L), action(1L)))).isInstanceOf(IllegalArgumentException.class)
                                                 .hasMessageContaining("Duplicate action identifier");
    }

    @Test
    void shouldPrepareDeepChains_withoutRecursiveStackGrowth() {
        List<Action> actions = new ArrayList<>();
        for (long id = 1; id < 10_000; id++)
            actions.add(action(id, id + 1));
        actions.add(action(10_000L));
        TestStep root = new TestStepArranger().testStepChain(actions);
        assertThat(root.getTestPathsFromThisStep()).singleElement()
                                                   .satisfies(path -> assertThat(path).hasSize(10_000));
    }

    private static Action action(Long actionId, Long... nextActionIds) {
        return ClickAction.builder()
                          .actionID(actionId)
                          .nextActions(List.of(nextActionIds))
                          .build();
    }

    private static Action actionWithoutNextActions(Long actionId) {
        return ClickAction.builder()
                          .actionID(actionId)
                          .nextActions(null)
                          .build();
    }

    @Test
    void shouldBuildEveryPath_whenTheActionGraphBranches() {
        TestStep testStep = new TestStepArranger().testStepChain(
                List.of(action(1L, 2L, 3L), action(2L, 4L), action(3L, 4L), action(4L)));

        assertThat(testStep.getTestPathsFromThisStep()).extracting(path -> path.stream()
                                                                               .map(step -> step.action()
                                                                                                .getActionID())
                                                                               .toList())
                                                       .containsExactlyInAnyOrder(List.of(1L, 2L, 4L),
                                                               List.of(1L, 3L, 4L));
    }

    @Test
    void shouldTreatAbsentNextActionsAsNoFollowingActions() {
        TestStep testStep = new TestStepArranger().testStepChain(List.of(actionWithoutNextActions(1L)));

        assertThat(testStep.nextTestSteps()).isEmpty();
    }

}
