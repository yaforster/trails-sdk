package io.github.yaforster.trails.app.services;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImmutableCollectionRequestTest {

    @Test
    void applicationsWithStages_shouldCopyApplicationIds() {
        List<Long> applicationIds = new ArrayList<>(List.of(1L));
        StageDatabaseService.ApplicationsWithStages applications = new StageDatabaseService.ApplicationsWithStages(
                applicationIds);
        applicationIds.add(2L);

        assertThat(applications.applicationIds()).containsExactly(1L);
    }

    @Test
    void findAllIdsByElementIdsRequest_shouldCopyElementIds() {
        List<Long> elementIds = new ArrayList<>(List.of(1L));
        ScreenshotDatabaseService.ElementScreenshotIds elementScreenshotIds = new ScreenshotDatabaseService.ElementScreenshotIds(
                elementIds);
        elementIds.add(2L);

        assertThat(elementScreenshotIds.elementIds()).containsExactly(1L);
    }

    @Test
    void listActionResultChainsRequest_shouldCopyPathResultIds() {
        List<Long> pathResultIds = new ArrayList<>(List.of(1L));
        ActionResultQueryService.ActionResultChains actionResultChains = new ActionResultQueryService.ActionResultChains(
                pathResultIds);
        pathResultIds.add(2L);

        assertThat(actionResultChains.pathResultIds()).containsExactly(1L);
    }

    @Test
    void findActionResultScreenshotsRequest_shouldCopyActionResultIds() {
        List<Long> actionResultIds = new ArrayList<>(List.of(1L));
        ActionResultScreenshotQueryService.ActionScreenshots actionScreenshots = new ActionResultScreenshotQueryService.ActionScreenshots(
                actionResultIds);
        actionResultIds.add(2L);

        assertThat(actionScreenshots.actionResultIds()).containsExactly(1L);
    }

}
