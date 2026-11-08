package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ClickAction extends WebpageAction {

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return clickElement(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, getTechnicalFailureMessage(), context);
        }
    }

    private Success clickElement(TestExecutionContext context) {
        String clickedElementTagName = context.browser()
                                              .click(getLocatorForElementToActOn());
        return clickedElement(context, clickedElementTagName);
    }

    private Success clickedElement(TestExecutionContext context, String clickedElementTagName) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage(clickedElementTagName + " was successfully clicked on.")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

}
