package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.action.browser.WebpageAction;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CheckExistenceAction extends WebpageAction {

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return checkElementExists(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, getTechnicalFailureMessage(), context);
        }
    }

    private Result checkElementExists(TestExecutionContext context) {
        if (context.browser()
                   .elementExists(getLocatorForElementToActOn())) {
            return elementFound(context);
        }
        return elementMissing(context);
    }

    private Success elementFound(TestExecutionContext context) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("Successfully located the required web page element.")
                      .base64Screenshot(captureScreenshotIfAvailable(context))
                      .build();
    }

    private ValidationFailure elementMissing(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage("The required web page element was not found.")
                                .label(getLabel())
                                .base64Screenshot(captureScreenshotIfAvailable(context))
                                .build();
    }

}
