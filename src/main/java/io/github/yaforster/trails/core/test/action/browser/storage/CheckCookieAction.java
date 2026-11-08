package io.github.yaforster.trails.core.test.action.browser.storage;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.Strings;
import java.util.Optional;

@SuperBuilder
@Getter
public class CheckCookieAction extends Action {

    private final String cookieName;
    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        Optional<String> cookieValue = context.browser()
                                              .cookieValue(cookieName);
        if (cookieValue.isEmpty()) {
            return cookieMissing(context);
        }
        return checkCookieValue(context, cookieValue.get());
    }

    private Result checkCookieValue(TestExecutionContext context, String cookieValue) {
        String expectedValue = context.compute(computationInstruction);
        if (Strings.CI.contains(cookieValue, expectedValue)) {
            return cookieContainsValue(context, cookieValue);
        }
        return cookieLacksValue(context);
    }

    private ValidationFailure cookieMissing(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage(cookieName + " does not exist.")
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

    private Success cookieContainsValue(TestExecutionContext context, String cookieValue) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage(cookieName + " does contain the target value. Complete content: " + cookieValue)
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private ValidationFailure cookieLacksValue(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage(cookieName + " exists but does not contain the target value.")
                                .label(getLabel())
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

}
