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

@SuperBuilder
@Getter
public class CheckSessionStorageAction extends Action {

    private final String sessionStorageItemKey;
    private final ValueComputationInstruction computationInstruction;

    @Override
    public Result execute(TestExecutionContext context) {
        String storedValue = context.browser()
                                    .sessionStorageValue(sessionStorageItemKey);
        if (StringUtils.isBlank(storedValue)) {
            return sessionStorageItemMissing(context);
        }
        return checkStoredValue(context, storedValue);
    }

    private Result checkStoredValue(TestExecutionContext context, String storedValue) {
        String expectedValue = context.compute(computationInstruction);
        if (Strings.CI.contains(storedValue, expectedValue)) {
            return sessionStorageContainsValue(context, storedValue);
        }
        return sessionStorageLacksValue(context);
    }

    private ValidationFailure sessionStorageItemMissing(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage(sessionStorageItemKey + " does not exist.")
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

    private Success sessionStorageContainsValue(TestExecutionContext context, String storedValue) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage(sessionStorageItemKey + " does contain the target value. Complete content: "
                              + storedValue)
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private ValidationFailure sessionStorageLacksValue(TestExecutionContext context) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .resultMessage(sessionStorageItemKey + " exists but does not contain the target value.")
                                .label(getLabel())
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

}
