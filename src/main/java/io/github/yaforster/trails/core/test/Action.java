package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public abstract class Action {

    private final Long actionID;
    private final String label;
    @Singular(value = "nextAction", ignoreNullCollections = true)
    private final List<Long> nextActions;
    private final Position position;

    public abstract Result execute(TestExecutionContext context);

    protected TechnicalFailure generateTechnicalFailureResult(Exception e, String message,
            TestExecutionContext context) {
        TechnicalFailure.TechnicalFailureBuilder<?, ?> builder = TechnicalFailure.builder()
                                                                                 .actionID(actionID)
                                                                                 .resultMessage(message)
                                                                                 .label(getLabel())
                                                                                 .exceptionMessageFromAction(
                                                                                         e.getMessage())
                                                                                 .base64Screenshot(
                                                                                         captureScreenshotIfAvailable(
                                                                                                 context));
        return builder.build();
    }

    protected String captureScreenshotIfAvailable(TestExecutionContext context) {
        try {
            return getPageScreenshot(context);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    protected String getPageScreenshot(TestExecutionContext context) {
        return context.browser()
                      .captureScreenshot();
    }

}
