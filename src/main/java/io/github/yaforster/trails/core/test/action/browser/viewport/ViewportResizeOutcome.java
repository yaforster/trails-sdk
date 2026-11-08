package io.github.yaforster.trails.core.test.action.browser.viewport;

import java.util.Objects;
import java.util.Optional;

public sealed interface ViewportResizeOutcome permits ViewportResizeOutcome.Success, ViewportResizeOutcome.Failure {

    ViewportDimensions requested();

    record Success(ViewportDimensions requested, ViewportDimensions measured) implements ViewportResizeOutcome {

        public Success {
            Objects.requireNonNull(requested, "Requested viewport dimensions are required.");
            Objects.requireNonNull(measured, "Measured viewport dimensions are required.");
            if (!requested.equals(measured)) {
                throw new IllegalArgumentException(
                        "Successful viewport resize must match requested dimensions exactly.");
            }
        }

    }

    record Failure(ViewportDimensions requested, Optional<ViewportDimensions> lastMeasured,
            String cause) implements ViewportResizeOutcome {

        public Failure {
            Objects.requireNonNull(requested, "Requested viewport dimensions are required.");
            lastMeasured = Objects.requireNonNull(lastMeasured, "Last measured viewport dimensions are required.");
            if (cause == null || cause.isBlank()) {
                throw new IllegalArgumentException("Viewport resize failure cause is required.");
            }
        }

    }

}
