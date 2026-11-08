package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class ResizeViewportActionTest {

    @Test
    void execute_returnsSuccessAfterExactViewportResizeAndCapturesFullPageProof() {
        ViewportDimensions dimensions = new ViewportDimensions(800, 600);
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().screenshot(
                "proof");

        Result result = action(dimensions).execute(TestExecutionContexts.withBrowser(browser));

        Success success = assertInstanceOf(Success.class, result);
        assertEquals(dimensions, browser.viewportResizeRequests()
                                        .getFirst());
        assertEquals("Viewport was resized to 800 x 600 CSS pixels.", success.getResultMessage());
        assertEquals("proof", success.getBase64Screenshot());
    }

    @Test
    void execute_returnsTechnicalFailureWithRequestedMeasuredAndCause() {
        ViewportDimensions requested = new ViewportDimensions(800, 600);
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().viewportResizeOutcome(
                new ViewportResizeOutcome.Failure(requested, Optional.of(new ViewportDimensions(799, 600)),
                        "Node clamped width."));

        TechnicalFailure failure = assertInstanceOf(TechnicalFailure.class,
                action(requested).execute(TestExecutionContexts.withBrowser(browser)));

        assertEquals(
                "Requested viewport: 800 x 600 CSS pixels. Last measured viewport: 799 x 600 CSS pixels. Cause: Node clamped width.",
                failure.getExceptionMessageFromAction());
    }

    @Test
    void execute_reportsUnavailableMeasurementForTypedFailure() {
        ViewportDimensions requested = new ViewportDimensions(800, 600);
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().viewportResizeOutcome(
                new ViewportResizeOutcome.Failure(requested, Optional.empty(), "JavaScript failed."))
                                                                                                         .screenshotFailure(
                                                                                                                 new IllegalStateException(
                                                                                                                         "proof failed"));

        TechnicalFailure failure = assertInstanceOf(TechnicalFailure.class,
                action(requested).execute(TestExecutionContexts.withBrowser(browser)));

        assertEquals(
                "Requested viewport: 800 x 600 CSS pixels. Last measured viewport: unavailable. Cause: JavaScript failed.",
                failure.getExceptionMessageFromAction());
        assertNull(failure.getBase64Screenshot());
    }

    @Test
    void execute_reportsRequestedViewportWhenBrowserPortThrows() {
        TechnicalFailure failure = assertInstanceOf(TechnicalFailure.class,
                action(new ViewportDimensions(800, 600)).execute(TestExecutionContexts.withBrowser(
                        new TestExecutionContexts.FakeBrowserSession().operationFailure(
                                new IllegalStateException("Port failed.")))));

        assertEquals(
                "Requested viewport: 800 x 600 CSS pixels. Last measured viewport: unavailable. Cause: Port failed.",
                failure.getExceptionMessageFromAction());
    }

    private static ResizeViewportAction action(ViewportDimensions dimensions) {
        return ResizeViewportAction.builder()
                                   .actionID(1L)
                                   .label("resize")
                                   .viewportDimensions(dimensions)
                                   .build();
    }

}
