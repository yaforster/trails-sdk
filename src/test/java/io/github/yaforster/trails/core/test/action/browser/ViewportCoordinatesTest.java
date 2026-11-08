package io.github.yaforster.trails.core.test.action.browser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViewportCoordinatesTest {

    @Test
    void constructor_acceptsNonNegativeCoordinates() {
        assertDoesNotThrow(() -> new ViewportCoordinates(0, Integer.MAX_VALUE));
    }

    @Test
    void constructor_rejectsNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new ViewportCoordinates(-1, 0));
    }

}
