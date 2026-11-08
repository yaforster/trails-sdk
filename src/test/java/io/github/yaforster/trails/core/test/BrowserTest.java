package io.github.yaforster.trails.core.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BrowserTest {

    @Test
    void parse_ShouldMapValidValue() {
        assertEquals(Browser.CHROME, Browser.parse("CHROME"));
        assertEquals(Browser.FIREFOX, Browser.parse("firefox"));
        assertEquals(Browser.EDGE, Browser.parse(" edge "));
    }

    @Test
    void parse_ShouldThrowForNull() {
        assertThrows(IllegalArgumentException.class, () -> Browser.parse(null));
    }

    @Test
    void parse_ShouldThrowForBlank() {
        assertThrows(IllegalArgumentException.class, () -> Browser.parse(" "));
    }

    @Test
    void parse_ShouldThrowForUnsupportedValue() {
        assertThrows(IllegalArgumentException.class, () -> Browser.parse("SAFARI"));
    }

}
