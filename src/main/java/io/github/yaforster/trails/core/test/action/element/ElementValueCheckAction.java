package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.Strings;

@SuperBuilder
@Getter
public class ElementValueCheckAction extends WebpageAction {

    private final ElementValueSource valueSource;
    private final String valueName;
    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return verifyElementContainsExpectedValue(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, getTechnicalFailureMessage(), context);
        }
    }

    private Result verifyElementContainsExpectedValue(TestExecutionContext context) {
        String actualValue = context.browser()
                                    .readValue(getLocatorForElementToActOn(), valueSource, valueName);
        String expectedValue = context.compute(computationInstruction);

        if (actualValue != null && Strings.CI.contains(actualValue, expectedValue)) {
            return valueFound(context, actualValue);
        }

        return valueMissing(context, actualValue);
    }

    private Success valueFound(TestExecutionContext context, String actualValue) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage(valueLabel() + " contains the target value. Complete content: " + actualValue)
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private ValidationFailure valueMissing(TestExecutionContext context, String actualValue) {
        String message;
        if (actualValue == null) {
            message = valueLabel() + " does not exist on the target element.";
        } else {
            message = valueLabel() + " exists but does not contain the target value.";
        }
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .label(getLabel())
                                .resultMessage(message)
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

    private String valueLabel() {
        return valueSource + " '" + valueName + "'";
    }

}
