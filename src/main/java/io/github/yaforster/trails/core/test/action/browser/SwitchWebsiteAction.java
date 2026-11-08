package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class SwitchWebsiteAction extends Action {

    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return navigateToComputedUrl(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, "Could not navigate to the target URL", context);
        }
    }

    private Success navigateToComputedUrl(TestExecutionContext context) {
        String urlToNavigateTo = context.compute(computationInstruction);
        context.browser()
               .open(urlToNavigateTo);
        return navigationSucceeded(context);
    }

    private Success navigationSucceeded(TestExecutionContext context) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("Successfully Navigated to the URL.")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

}
