package io.github.yaforster.trails.core.test.action.browser;

public record ViewportCoordinates(int xCoordinate, int yCoordinate) {

    public ViewportCoordinates {
        if (xCoordinate < 0 || yCoordinate < 0) {
            throw new IllegalArgumentException("Viewport coordinates must not be negative.");
        }
    }

}
