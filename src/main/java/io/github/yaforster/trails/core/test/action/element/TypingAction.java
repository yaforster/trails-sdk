package io.github.yaforster.trails.core.test.action.element;

import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.action.browser.WebpageAction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter

@SuperBuilder
public class TypingAction extends WebpageAction {

    private final ValueComputationInstruction computationInstruction;
    private final boolean clearBeforeTyping;
    private final long delayAfterClearMillis;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return typeComputedValue(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, getTechnicalFailureMessage(), context);
        }
    }

    private Success typeComputedValue(TestExecutionContext context) {
        String charactersToEnter = context.compute(computationInstruction);
        String typedElementTagName = context.browser()
                                            .type(getLocatorForElementToActOn(), charactersToEnter, clearBeforeTyping,
                                                    delayAfterClearMillis);
        return typedValue(context, typedElementTagName);
    }

    private Success typedValue(TestExecutionContext context, String typedElementTagName) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("Value was successfully typed into " + typedElementTagName + ".")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

}
