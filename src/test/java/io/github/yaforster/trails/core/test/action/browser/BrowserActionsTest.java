package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.action.browser.viewport.MoveViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class BrowserActionsTest {

    @Test
    void explicitWait_returnsConfiguredResultAndScreenshot() {
        Result result = ExplicitWaitAction.builder()
                                          .actionID(10L)
                                          .label("wait")
                                          .delayMillis(0)
                                          .build()
                                          .execute(TestExecutionContexts.withBrowser(
                                                  new TestExecutionContexts.FakeBrowserSession().screenshot("shot")));

        assertEquals("shot", result.getBase64Screenshot());
    }

    @Test
    void switchWebsite_opensComputedUrlThroughBrowserPort() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession();

        SwitchWebsiteAction.builder()
                           .actionID(11L)
                           .label("navigate")
                           .nextActions(List.of())
                           .computationInstruction(new FixedValueInstruction("https://example.test"))
                           .build()
                           .execute(TestExecutionContexts.withBrowser(browser));

        assertEquals(List.of("https://example.test"), browser.openedUrls());
    }

    @Test
    void switchWebsite_returnsTechnicalFailure_whenNavigationFails() {
        Result result = SwitchWebsiteAction.builder()
                                           .actionID(11L)
                                           .label("navigate")
                                           .nextActions(List.of())
                                           .computationInstruction(new FixedValueInstruction("https://example.test"))
                                           .build()
                                           .execute(TestExecutionContexts.withBrowser(
                                                   new TestExecutionContexts.FakeBrowserSession().operationFailure(
                                                           new IllegalStateException("offline"))));

        assertInstanceOf(TechnicalFailure.class, result);
    }

    @Test
    void moveViewport_normalizesNegativeAmountBeforeDelegating() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession();

        MoveViewportAction.builder()
                          .actionID(12L)
                          .label("move")
                          .movement(ViewportMove.SCROLL_BY)
                          .direction(ViewportMoveDirection.UP)
                          .amount(-2D)
                          .unit(ViewportMoveUnit.VIEWPORTS)
                          .delayAfterMoveMillis(0)
                          .build()
                          .execute(TestExecutionContexts.withBrowser(browser));

        assertEquals(0D, browser.viewportMoves()
                                .getFirst()
                                .amount());
    }

    @Test
    void moveViewport_returnsTechnicalFailure_whenBrowserPortFails() {
        Result result = MoveViewportAction.builder()
                                          .actionID(12L)
                                          .label("move")
                                          .movement(ViewportMove.PAGE_FLIP)
                                          .direction(ViewportMoveDirection.DOWN)
                                          .amount(1D)
                                          .unit(ViewportMoveUnit.VIEWPORTS)
                                          .delayAfterMoveMillis(0)
                                          .build()
                                          .execute(TestExecutionContexts.withBrowser(
                                                  new TestExecutionContexts.FakeBrowserSession().operationFailure(
                                                          new IllegalStateException("scroll failed"))));

        assertInstanceOf(TechnicalFailure.class, result);
    }

}
