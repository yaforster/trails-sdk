package io.github.yaforster.trails.sdk.host.print;

import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.PrintFacadeService;
import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.test.Browser;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PrintFacadeTest {
    @Test
    void shouldReturnNoDocument_whenPathInventoryIsUnavailable() {
        TestSetResultQueryService sets = mock(TestSetResultQueryService.class);
        TestPathResultQueryService paths = mock(TestPathResultQueryService.class);
        when(sets.getTestSetResult(11L)).thenReturn(Optional.of(testSet()));
        PrintFacade facade = facade(sets, paths, mock(ActionResultQueryService.class));

        assertThat(facade.printTestSet(new PrintFacadeService.TestSetPrintData(1L, 2L, 3L, 11L))).isEmpty();
    }

    @Test
    void shouldReturnNoDocument_whenChainInventoryContainsUnexpectedPath() {
        TestSetResultQueryService sets = mock(TestSetResultQueryService.class);
        TestPathResultQueryService paths = mock(TestPathResultQueryService.class);
        ActionResultQueryService actions = mock(ActionResultQueryService.class);
        when(sets.getTestSetResult(11L)).thenReturn(Optional.of(testSet()));
        when(paths.listPathResults(any(TestPathResultQueryService.TestSetPaths.class))).thenReturn(
                PagedResult.singlePage(List.of(new PersistedTestPathResult(21L, 11L, "path"))));
        when(actions.listActionResultChains(any(ActionResultQueryService.ActionResultChains.class))).thenReturn(
                Map.of(22L, List.of()));
        PrintFacade facade = facade(sets, paths, actions);

        assertThat(facade.printTestSet(new PrintFacadeService.TestSetPrintData(1L, 2L, 3L, 11L))).isEmpty();
    }

    @Test
    void shouldAllowEmptyPathInventory_whenBrowserSetHasNoPaths() {
        TestSetResultQueryService sets = mock(TestSetResultQueryService.class);
        TestPathResultQueryService paths = mock(TestPathResultQueryService.class);
        when(sets.getTestSetResult(11L)).thenReturn(Optional.of(testSet()));
        when(paths.listPathResults(any(TestPathResultQueryService.TestSetPaths.class))).thenReturn(
                PagedResult.singlePage(List.of()));
        PrintFacade facade = facade(sets, paths, mock(ActionResultQueryService.class));

        assertThat(facade.printTestSet(new PrintFacadeService.TestSetPrintData(1L, 2L, 3L, 11L))).isPresent();
    }

    @Test
    void shouldReturnNoDocument_whenPathInventoryIncludesAnotherBrowserSet() {
        TestSetResultQueryService sets = mock(TestSetResultQueryService.class);
        TestPathResultQueryService paths = mock(TestPathResultQueryService.class);
        when(sets.getTestSetResult(11L)).thenReturn(Optional.of(testSet()));
        when(paths.listPathResults(any(TestPathResultQueryService.TestSetPaths.class))).thenReturn(
                PagedResult.singlePage(List.of(new PersistedTestPathResult(21L, 12L, "foreign"))));
        PrintFacade facade = facade(sets, paths, mock(ActionResultQueryService.class));

        assertThat(facade.printTestSet(new PrintFacadeService.TestSetPrintData(1L, 2L, 3L, 11L))).isEmpty();
    }

    private static PersistedTestSetResult testSet() {
        return new PersistedTestSetResult(11L, 3L, 1L, 2L, 0, Browser.CHROME, "plan", null, Browser.CHROME);
    }

    private static PrintFacade facade(TestSetResultQueryService sets, TestPathResultQueryService paths,
            ActionResultQueryService actions) {
        HierarchyValidationService hierarchies = mock(HierarchyValidationService.class);
        when(hierarchies.testSetBelongsToRun(any(HierarchyValidationService.TestSetHierarchy.class))).thenReturn(true);
        return new PrintFacade(sets, paths, actions, mock(TestRunResultDatabaseService.class), mock(PrintService.class),
                hierarchies);
    }
}
