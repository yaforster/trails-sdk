package io.github.yaforster.trails.core.test.action.browser.viewport;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViewportDimensionsTest {

    @Test
    void constructor_acceptsPositiveInt32Bounds() {
        assertDoesNotThrow(() -> new ViewportDimensions(1, Integer.MAX_VALUE));
    }

    @Test
    void constructor_rejectsNonPositiveDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new ViewportDimensions(0, -1));
    }

    @Test
    void successOutcome_rejectsMismatchedMeasurement() {
        assertThrows(IllegalArgumentException.class,
                () -> new ViewportResizeOutcome.Success(new ViewportDimensions(1, 1), new ViewportDimensions(2, 1)));
    }

    @Test
    void failureOutcome_rejectsMissingCause() {
        assertThrows(IllegalArgumentException.class,
                () -> new ViewportResizeOutcome.Failure(new ViewportDimensions(1, 1), Optional.empty(), ""));
    }

}
