package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class ExplicitWaitAction extends Action {

    private final long delayMillis;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return waitForConfiguredDelay(context);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, "Could not wait for the configured delay.", context);
        }
    }

    private Success waitForConfiguredDelay(TestExecutionContext context) throws InterruptedException {
        long safeDelayMillis = Math.max(0L, delayMillis);

        if (safeDelayMillis > 0) {
            try {
                Thread.sleep(safeDelayMillis);
            } catch (InterruptedException interruption) {
                Thread.currentThread()
                      .interrupt();
                throw interruption;
            }
        }

        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("Waited for " + safeDelayMillis + " milliseconds.")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

}
