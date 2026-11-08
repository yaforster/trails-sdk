package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class CoordinateClickAction extends Action {

    private final ViewportCoordinates viewportCoordinates;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            context.browser()
                   .click(viewportCoordinates);
            return Success.builder()
                          .actionID(getActionID())
                          .label(getLabel())
                          .resultMessage(successMessage())
                          .base64Screenshot(captureScreenshotIfAvailable(context))
                          .build();
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, failureMessage(), context);
        }
    }

    private String successMessage() {
        return "Viewport coordinates (" + viewportCoordinates.xCoordinate() + ", " + viewportCoordinates.yCoordinate()
                + ") were successfully clicked.";
    }

    private String failureMessage() {
        return "Could not click viewport coordinates (" + viewportCoordinates.xCoordinate() + ", "
                + viewportCoordinates.yCoordinate() + ").";
    }

}
