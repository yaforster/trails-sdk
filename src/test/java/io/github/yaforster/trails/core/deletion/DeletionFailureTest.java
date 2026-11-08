package io.github.yaforster.trails.core.deletion;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DeletionFailureTest {

    @Test
    void shouldUseSafeMessage_whenCreatedFromException() {
        DeletionFailure failure = new DeletionFailure(1L,
                new IllegalStateException("io.github.yaforster.trails.DatabaseService failed"));

        assertEquals("The requested item could not be deleted.", failure.message());
    }

}
