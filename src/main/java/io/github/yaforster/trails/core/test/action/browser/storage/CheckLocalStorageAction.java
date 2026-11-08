package io.github.yaforster.trails.core.test.action.browser.storage;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

@Getter
@SuperBuilder

public class CheckLocalStorageAction extends Action {

    private final String localStorageItemKey;
    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        String storedValue = context.browser()
                                    .localStorageValue(localStorageItemKey);
        if (StringUtils.isBlank(storedValue)) {
            return localStorageItemMissing();
        }
        return checkStoredValue(context, storedValue);
    }

    private Result checkStoredValue(TestExecutionContext context, String storedValue) {
        String expectedValue = context.compute(computationInstruction);
        if (Strings.CI.contains(storedValue, expectedValue)) {
            return localStorageContainsValue(context, storedValue);
        }
        return localStorageLacksValue(context);
    }

    private ValidationFailure localStorageItemMissing() {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage(localStorageItemKey + " does not exist.")
                                .build();
    }

    private Success localStorageContainsValue(TestExecutionContext context, String storedValue) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage(
                              localStorageItemKey + " does contain the target value. Complete content: " + storedValue)
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private ValidationFailure localStorageLacksValue(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage(localStorageItemKey + " exists but does not contain the target value.")
                                .label(getLabel())
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

}
