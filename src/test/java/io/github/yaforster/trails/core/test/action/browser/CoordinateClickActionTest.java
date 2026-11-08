package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoordinateClickActionTest {

    private static CoordinateClickAction action() {
        return CoordinateClickAction.builder()
                                    .actionID(1L)
                                    .label("coordinate click")
                                    .viewportCoordinates(new ViewportCoordinates(12, 34))
                                    .build();
    }

    @Test
    void execute_clicksCoordinatesAndCapturesFullPageProof() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().screenshot(
                "proof");

        Result result = action().execute(TestExecutionContexts.withBrowser(browser));

        Success success = assertInstanceOf(Success.class, result);
        assertEquals(new ViewportCoordinates(12, 34), browser.coordinateClicks()
                                                             .getFirst());
        assertEquals("proof", success.getBase64Screenshot());
        assertEquals("Viewport coordinates (12, 34) were successfully clicked.", success.getResultMessage());
    }

    @Test
    void execute_returnsSuccessWhenProofCaptureFails() {
        Result result = action().execute(
                TestExecutionContexts.withBrowser(new TestExecutionContexts.FakeBrowserSession().screenshotFailure(
                        new IllegalStateException("proof failed"))));

        Success success = assertInstanceOf(Success.class, result);
        assertNull(success.getBase64Screenshot());
    }

    @Test
    void execute_returnsTechnicalFailureWhenBrowserClickFails() {
        Result result = action().execute(
                TestExecutionContexts.withBrowser(new TestExecutionContexts.FakeBrowserSession().operationFailure(
                        new IllegalStateException("click failed"))));

        TechnicalFailure failure = assertInstanceOf(TechnicalFailure.class, result);
        assertEquals("Could not click viewport coordinates (12, 34).", failure.getResultMessage());
    }

    @Test
    void execute_returnsTechnicalFailureWhenClickAndProofCaptureFail() {
        Result result = action().execute(TestExecutionContexts.withBrowser(
                new TestExecutionContexts.FakeBrowserSession().operationFailure(
                        new IllegalStateException("click failed"))
                                                              .screenshotFailure(
                                                                      new IllegalStateException("proof failed"))));

        TechnicalFailure failure = assertInstanceOf(TechnicalFailure.class, result);
        assertNull(failure.getBase64Screenshot());
    }

}
