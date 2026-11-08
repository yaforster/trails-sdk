package io.github.yaforster.trails.app.services;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestPathResultQueryServiceTest {

    @Test
    void listPathResultsForTestSetsRequest_shouldCopyTestSetResultIds() {
        List<Long> testSetResultIds = new ArrayList<>(List.of(1L));
        TestPathResultQueryService.TestSetPaths testSetPaths = new TestPathResultQueryService.TestSetPaths(
                testSetResultIds, 0, 20);
        testSetResultIds.add(2L);

        assertThat(testSetPaths.testSetResultIds()).containsExactly(1L);
    }

}
