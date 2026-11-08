package io.github.yaforster.trails.core.test.action.browser;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.Optional;

@Getter
@SuperBuilder
public class ResizeViewportAction extends Action {

    private final ViewportDimensions viewportDimensions;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            ViewportResizeOutcome outcome = context.browser()
                                                   .resizeViewport(viewportDimensions);
            if (outcome == null) {
                throw new IllegalStateException("Browser did not return a viewport resize outcome.");
            }
            return switch (outcome) {
                case ViewportResizeOutcome.Success success -> successfulResult(success, context);
                case ViewportResizeOutcome.Failure failure -> technicalFailure(failure, context);
            };
        } catch (Exception exception) {
            return technicalFailure(
                    new ViewportResizeOutcome.Failure(viewportDimensions, Optional.empty(), failureCause(exception)),
                    context);
        }
    }

    private Success successfulResult(ViewportResizeOutcome.Success success, TestExecutionContext context) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("Viewport was resized to " + dimensions(success.measured()) + ".")
                      .base64Screenshot(captureScreenshotIfAvailable(context))
                      .build();
    }

    private TechnicalFailure technicalFailure(ViewportResizeOutcome.Failure failure, TestExecutionContext context) {
        return TechnicalFailure.builder()
                               .actionID(getActionID())
                               .label(getLabel())
                               .resultMessage("Could not resize viewport to " + dimensions() + ".")
                               .exceptionMessageFromAction(diagnostic(failure))
                               .base64Screenshot(captureScreenshotIfAvailable(context))
                               .build();
    }

    private String diagnostic(ViewportResizeOutcome.Failure failure) {
        String measured = failure.lastMeasured()
                                 .map(this::dimensions)
                                 .orElse("unavailable");
        return "Requested viewport: " + dimensions(failure.requested()) + ". Last measured viewport: " + measured
                + ". Cause: " + failure.cause();
    }

    private String dimensions() {
        return dimensions(viewportDimensions);
    }

    private String dimensions(ViewportDimensions dimensions) {
        return dimensions.width() + " x " + dimensions.height() + " CSS pixels";
    }

    private String failureCause(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "Browser did not provide a cause.";
        }
        return message;
    }

}
