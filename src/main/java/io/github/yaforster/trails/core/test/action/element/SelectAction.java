package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class SelectAction extends WebpageAction {

    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return selectComputedValue(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, getTechnicalFailureMessage(), context);
        }
    }

    private Success selectComputedValue(TestExecutionContext context) {
        String labelToSelect = context.compute(computationInstruction);
        String selectElementTagName = context.browser()
                                             .select(getLocatorForElementToActOn(), labelToSelect);
        return selectedValue(context, selectElementTagName);
    }

    private Success selectedValue(TestExecutionContext context, String selectElementTagName) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("The target value was successfully selected from " + selectElementTagName + ".")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

}
