package io.github.yaforster.trails.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class PagedResultTest {

    @Test
    void totalPages_rejectsCountsThatCannotFitTheApiRepresentation() {
        PagedResult<String> result = new PagedResult<>(List.of(), 0, Integer.MAX_VALUE, Long.MAX_VALUE);

        assertThrows(ArithmeticException.class, result::totalPages);
    }

}
