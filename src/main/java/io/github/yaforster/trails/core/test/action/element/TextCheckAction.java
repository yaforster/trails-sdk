package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.BrowserElementText;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder

@Getter
public class TextCheckAction extends WebpageAction {

    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return checkElementText(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, getTechnicalFailureMessage(), context);
        }
    }

    private Result checkElementText(TestExecutionContext context) {
        String textToSearchFor = context.compute(computationInstruction);
        BrowserElementText elementText = context.browser()
                                                .readText(getLocatorForElementToActOn());
        if (elementText.text()
                       .contains(textToSearchFor)) {
            return textFound(context, elementText.tagName());
        } else {
            return textMissing(context);
        }
    }

    private Success textFound(TestExecutionContext context, String textElementTagName) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("The search string was successfully located in " + textElementTagName + ".")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private ValidationFailure textMissing(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .label(getLabel())
                                .resultMessage("Text element was found but does not contain the search string.")
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

}
