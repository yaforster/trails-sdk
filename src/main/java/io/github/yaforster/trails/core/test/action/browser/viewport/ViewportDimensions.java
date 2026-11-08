package io.github.yaforster.trails.core.test.action.browser.viewport;

public record ViewportDimensions(int width, int height) {

    public ViewportDimensions {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Viewport dimensions must be positive.");
        }
    }

}
