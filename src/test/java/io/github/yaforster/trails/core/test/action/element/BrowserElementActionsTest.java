package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.BrowserElementText;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

class BrowserElementActionsTest {

    private static final Locator LOCATOR = new Locator(LocatorType.XPATH, "//input");

    @Test
    void checkExistence_returnsSuccess_whenPortFindsElement() {
        Result result = CheckExistenceAction.builder()
                                            .actionID(1L)
                                            .label("exists")
                                            .nextActions(List.of())
                                            .locatorForElementToActOn(LOCATOR)
                                            .build()
                                            .execute(TestExecutionContexts.withBrowser(
                                                    new TestExecutionContexts.FakeBrowserSession().elementExists(
                                                            true)));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void checkExistence_returnsValidationFailure_whenPortDoesNotFindElement() {
        ValidationFailure result = assertInstanceOf(ValidationFailure.class, CheckExistenceAction.builder()
                                                                                                 .actionID(1L)
                                                                                                 .label("exists")
                                                                                                 .nextActions(List.of())
                                                                                                 .locatorForElementToActOn(
                                                                                                         LOCATOR)
                                                                                                 .build()
                                                                                                 .execute(
                                                                                                         TestExecutionContexts.withBrowser(
                                                                                                                 new TestExecutionContexts.FakeBrowserSession())));

        assertEquals("The required web page element was not found.", result.getResultMessage());
    }

    @Test
    void checkExistence_returnsTechnicalFailure_whenPortFailsAndScreenshotFails() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().elementExistsFailure(
                new IllegalStateException("lookup failed"))
                                                                                                         .screenshotFailure(
                                                                                                                 new IllegalStateException(
                                                                                                                         "screenshot failed"));

        TechnicalFailure result = assertInstanceOf(TechnicalFailure.class, CheckExistenceAction.builder()
                                                                                               .actionID(1L)
                                                                                               .label("exists")
                                                                                               .nextActions(List.of())
                                                                                               .locatorForElementToActOn(
                                                                                                       LOCATOR)
                                                                                               .build()
                                                                                               .execute(
                                                                                                       TestExecutionContexts.withBrowser(
                                                                                                               browser)));

        assertNull(result.getBase64Screenshot());
    }

    @Test
    void click_returnsTagNameAndScreenshot_fromBrowserPort() {
        Result result = ClickAction.builder()
                                   .actionID(2L)
                                   .label("click")
                                   .nextActions(List.of())
                                   .locatorForElementToActOn(LOCATOR)
                                   .build()
                                   .execute(TestExecutionContexts.withBrowser(
                                           new TestExecutionContexts.FakeBrowserSession().clickTagName("button")
                                                                                         .screenshot("shot")));

        assertEquals("button was successfully clicked on.", result.getResultMessage());
    }

    @Test
    void click_returnsTechnicalFailure_whenPortFails() {
        TechnicalFailure result = assertInstanceOf(TechnicalFailure.class, ClickAction.builder()
                                                                                      .actionID(2L)
                                                                                      .label("click")
                                                                                      .nextActions(List.of())
                                                                                      .locatorForElementToActOn(LOCATOR)
                                                                                      .build()
                                                                                      .execute(
                                                                                              TestExecutionContexts.withBrowser(
                                                                                                      new TestExecutionContexts.FakeBrowserSession().operationFailure(
                                                                                                              new IllegalStateException(
                                                                                                                      "io.github.yaforster.trails.Selenium failed")))));

        assertEquals("Could not interact with the required web page element.", result.getResultMessage());
    }

    @Test
    void typing_delegatesConfiguredClearAndDelay_toBrowserPort() {
        Result result = TypingAction.builder()
                                    .actionID(3L)
                                    .label("type")
                                    .nextActions(List.of())
                                    .locatorForElementToActOn(LOCATOR)
                                    .computationInstruction(new FixedValueInstruction("value"))
                                    .clearBeforeTyping(true)
                                    .delayAfterClearMillis(0)
                                    .build()
                                    .execute(TestExecutionContexts.withBrowser(
                                            new TestExecutionContexts.FakeBrowserSession().typedTagName("textarea")));

        assertEquals("Value was successfully typed into textarea.", result.getResultMessage());
    }

    @Test
    void textCheck_returnsValidationFailure_whenTextDoesNotContainExpectedValue() {
        Result result = TextCheckAction.builder()
                                       .actionID(4L)
                                       .label("text")
                                       .nextActions(List.of())
                                       .locatorForElementToActOn(LOCATOR)
                                       .computationInstruction(new FixedValueInstruction("expected"))
                                       .build()
                                       .execute(TestExecutionContexts.withBrowser(
                                               new TestExecutionContexts.FakeBrowserSession().elementText(
                                                       new BrowserElementText("p", "actual"))));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void shouldReportSuccessfulTextCheckWithoutStrayPunctuation() {
        Result result = TextCheckAction.builder()
                                       .actionID(4L)
                                       .label("text")
                                       .nextActions(List.of())
                                       .locatorForElementToActOn(LOCATOR)
                                       .computationInstruction(new FixedValueInstruction("expected"))
                                       .build()
                                       .execute(TestExecutionContexts.withBrowser(
                                               new TestExecutionContexts.FakeBrowserSession().elementText(
                                                       new BrowserElementText("p", "expected text"))));

        assertEquals("The search string was successfully located in p.", result.getResultMessage());
    }

    @Test
    void elementValueCheck_readsConfiguredValueSourceThroughBrowserPort() {
        Result result = ElementValueCheckAction.builder()
                                               .actionID(5L)
                                               .label("attribute")
                                               .nextActions(List.of())
                                               .locatorForElementToActOn(LOCATOR)
                                               .valueSource(ElementValueSource.ATTRIBUTE)
                                               .valueName("data-state")
                                               .computationInstruction(new FixedValueInstruction("ready"))
                                               .build()
                                               .execute(TestExecutionContexts.withBrowser(
                                                       new TestExecutionContexts.FakeBrowserSession().elementValue(
                                                               "is-ready")));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void elementValueCheck_returnsValidationFailure_whenValueIsMissing() {
        Result result = ElementValueCheckAction.builder()
                                               .actionID(5L)
                                               .label("attribute")
                                               .nextActions(List.of())
                                               .locatorForElementToActOn(LOCATOR)
                                               .valueSource(ElementValueSource.PROPERTY)
                                               .valueName("checked")
                                               .computationInstruction(new FixedValueInstruction("true"))
                                               .build()
                                               .execute(TestExecutionContexts.withBrowser(
                                                       new TestExecutionContexts.FakeBrowserSession()));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void select_returnsSelectedControlTagName_fromBrowserPort() {
        Result result = SelectAction.builder()
                                    .actionID(6L)
                                    .label("select")
                                    .nextActions(List.of())
                                    .locatorForElementToActOn(LOCATOR)
                                    .computationInstruction(new FixedValueInstruction("Option A"))
                                    .build()
                                    .execute(TestExecutionContexts.withBrowser(
                                            new TestExecutionContexts.FakeBrowserSession().selectedTagName(
                                                    "p-select")));

        assertEquals("The target value was successfully selected from p-select.", result.getResultMessage());
    }

    @Test
    void select_returnsTechnicalFailure_whenBrowserPortCannotSelect() {
        Result result = SelectAction.builder()
                                    .actionID(6L)
                                    .label("select")
                                    .nextActions(List.of())
                                    .locatorForElementToActOn(LOCATOR)
                                    .computationInstruction(new FixedValueInstruction("Option A"))
                                    .build()
                                    .execute(TestExecutionContexts.withBrowser(
                                            new TestExecutionContexts.FakeBrowserSession().operationFailure(
                                                    new IllegalStateException("not found"))));

        assertInstanceOf(TechnicalFailure.class, result);
    }

}
