package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.action.element.CheckExistenceAction;
import io.github.yaforster.trails.core.test.action.element.Locator;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.core.test.action.element.TypingAction;
import io.github.yaforster.trails.core.test.action.value.RelativeDateInstruction;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class TestCaseTest {

    @Test
    void shouldRecordTechnicalFailure_whenBrowserProviderHasNoSession() throws Exception {
        TestCase testCase = new TestCase(new URL("https://example.test"), steps());

        TestPathResult result = testCase.runTestsUntilFailure(() -> Optional.empty(), List.of(steps()));

        assertInstanceOf(TechnicalFailure.class, result.results()
                                                       .getFirst());
    }

    @Test
    void runTestsUntilFailure_recordsTechnicalFailure_whenNavigationFails() throws Exception {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().operationFailure(
                new IllegalStateException("unavailable"));
        BrowserExecution execution = new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution());
        TestCase testCase = new TestCase(new URL("https://example.test"), steps());

        TestPathResult result = testCase.runTestsUntilFailure(() -> Optional.of(execution), List.of(steps()));

        assertInstanceOf(TechnicalFailure.class, result.results()
                                                       .getFirst());
    }

    @Test
    void runTestsUntilFailure_cleansUpDownloadsAndBrowser_whenNavigationFails() throws Exception {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().operationFailure(
                new IllegalStateException("unavailable"));
        BrowserExecution execution = new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution());
        TestCase testCase = new TestCase(new URL("https://example.test"), steps());

        testCase.runTestsUntilFailure(() -> Optional.of(execution), List.of(steps()));

        assertTrue(browser.downloadsCleared() && browser.closed());
    }

    @Test
    void runTestsUntilFailure_opensTheConfiguredApplicationUrl() throws Exception {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession();
        BrowserExecution execution = new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution());
        TestCase testCase = new TestCase(new URL("https://example.test"), steps());

        testCase.runTestsUntilFailure(() -> Optional.of(execution), List.of(steps()));

        assertEquals(List.of("https://example.test"), browser.openedUrls());
    }

    @Test
    void shouldUseInjectedClock_whenRecordingCaseResult() throws Exception {
        Instant instant = Instant.parse("2026-11-02T10:15:30Z");
        TestCase testCase = new TestCase(new URL("https://example.test"), steps(),
                Clock.fixed(instant, ZoneOffset.UTC));

        TestCaseResult result = testCase.run(() -> Optional.empty(), Browser.CHROME);

        assertEquals(Timestamp.from(instant), result.timestamp());
    }

    @Test
    void shouldComputeActionValuesWithInjectedExecutionClock() throws Exception {
        Locator locator = new Locator(LocatorType.CSS, "#date");
        TypingAction action = TypingAction.builder()
                                          .actionID(1L)
                                          .label("date")
                                          .nextActions(List.of())
                                          .locatorForElementToActOn(locator)
                                          .computationInstruction(new RelativeDateInstruction(0, "uuuu-MM-dd"))
                                          .build();
        TestStep step = new TestStep(action, List.of());
        Clock clock = Clock.fixed(Instant.parse("2026-11-05T23:30:00Z"), ZoneId.of("Asia/Tokyo"));
        TestCase testCase = new TestCase(new URL("https://example.test"), step, clock);
        BrowserSession browser = mock(BrowserSession.class);

        testCase.runTestsUntilFailure(
                () -> Optional.of(new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution())),
                List.of(step));

        verify(browser).type(locator, "2026-11-06", false, 0L);
    }

    @Test
    void shouldCloseBrowser_whenDownloadCleanupFails() throws Exception {
        BrowserSession browser = mock(BrowserSession.class);
        doThrow(new IllegalStateException("cleanup failed")).when(browser)
                                                            .clearDownloadedFiles();
        TestCase testCase = new TestCase(new URL("https://example.test"), steps());

        testCase.runTestsUntilFailure(
                () -> Optional.of(new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution())),
                List.of(steps()));

        verify(browser).close();
    }

    @Test
    void shouldRetainEarlierPaths_whenLaterBrowserCreationFails() throws Exception {
        TestStep first = steps();
        TestStep second = steps();
        TestStep start = new TestStep(steps().action(), List.of(first, second));
        TestCase testCase = new TestCase(new URL("https://example.test"), start);
        BrowserSession browser = mock(BrowserSession.class);
        DriverProvider provider = mock(DriverProvider.class);
        when(provider.createDriver()).thenReturn(
                Optional.of(new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution())))
                                     .thenThrow(new IllegalStateException("unavailable"));

        TestCaseResult result = testCase.run(provider, Browser.CHROME);

        assertEquals(2, result.results()
                              .size());
        assertInstanceOf(TechnicalFailure.class, result.results()
                                                       .getLast()
                                                       .results()
                                                       .getFirst());
    }

    @Test
    void shouldPreserveCompletedActions_whenLaterActionThrows() throws Exception {
        Action completed = mock(Action.class);
        Action failing = mock(Action.class);
        Result completedResult = mock(Result.class);
        when(completed.execute(any())).thenReturn(completedResult);
        when(failing.execute(any())).thenThrow(new IllegalStateException("unavailable"));
        TestStep last = new TestStep(failing, List.of());
        TestStep first = new TestStep(completed, List.of(last));
        TestCase testCase = new TestCase(new URL("https://example.test"), first);
        BrowserSession browser = mock(BrowserSession.class);
        DriverProvider provider = () -> Optional.of(
                new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution()));

        TestPathResult result = testCase.runTestsUntilFailure(provider, List.of(first, last));

        assertEquals(completedResult, result.results()
                                            .getFirst());
        assertInstanceOf(TechnicalFailure.class, result.results()
                                                       .getLast());
    }

    @Test
    void shouldPreserveCompletedActionsAndRecordFailure_whenDownloadInventoryFails() throws Exception {
        Action action = mock(Action.class);
        Result completed = mock(Result.class);
        when(action.execute(any())).thenReturn(completed);
        TestStep step = new TestStep(action, List.of());
        TestCase testCase = new TestCase(new URL("https://example.test"), step);
        BrowserSession browser = mock(BrowserSession.class);
        when(browser.refreshDownloadedFiles()).thenThrow(new IllegalStateException("download inventory unavailable"));
        DriverProvider provider = () -> Optional.of(
                new BrowserExecution(browser, new TestExecutionContexts.FakeDocumentExecution()));

        TestPathResult result = testCase.runTestsUntilFailure(provider, List.of(step));

        assertEquals(completed, result.results()
                                      .getFirst());
        assertInstanceOf(TechnicalFailure.class, result.results()
                                                       .getLast());
    }

    private static TestStep steps() {
        CheckExistenceAction action = CheckExistenceAction.builder()
                                                          .actionID(1L)
                                                          .label("exists")
                                                          .nextActions(List.of())
                                                          .locatorForElementToActOn(
                                                                  new Locator(LocatorType.CSS, "#target"))
                                                          .build();
        return new TestStep(action, List.of());
    }

}
