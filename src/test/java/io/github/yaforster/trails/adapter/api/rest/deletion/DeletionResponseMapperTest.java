package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeletionResponseMapperTest {

    private static final Map<String, LinkDTO> NOT_FOUND_LINKS = Map.of("collection",
            new LinkDTO().href("/api/applications?page=0&size=20&includeRetired=false")
                         .method("GET")
                         .templated(false));

    @Test
    void shouldMapSuccessfulDeletionWithoutRecoveryLinks() {
        var response = DeletionResponseMapper.toDto(new DeletionSuccess(12L), NOT_FOUND_LINKS);

        assertTrue(response.getDeletionResult());
        assertEquals(12L, response.getDeletionRequestedForID());
        assertEquals("Database entry was successfully deleted.", response.getMessage());
        assertTrue(response.getLinks()
                           .isEmpty());
        assertEquals(HttpStatus.OK, DeletionResponseMapper.status(new DeletionSuccess(12L)));
    }

    @Test
    void shouldMapMissingDeletionToNotFoundWithRecoveryLinks() {
        var response = DeletionResponseMapper.toDto(new DeletionNotFound(12L), NOT_FOUND_LINKS);

        assertFalse(response.getDeletionResult());
        assertEquals("/api/applications?page=0&size=20&includeRetired=false", response.getLinks()
                                                                                      .get("collection")
                                                                                      .getHref());
        assertEquals(HttpStatus.NOT_FOUND, DeletionResponseMapper.status(new DeletionNotFound(12L)));
    }

    @Test
    void shouldMapConflictDeletionWithoutTechnicalDetails() {
        var response = DeletionResponseMapper.toDto(new DeletionFailure(12L, "conflict", null), NOT_FOUND_LINKS);

        assertFalse(response.getDeletionResult());
        assertEquals("conflict", response.getMessage());
        assertTrue(response.getLinks()
                           .isEmpty());
        assertEquals(HttpStatus.CONFLICT, DeletionResponseMapper.status(new DeletionFailure(12L, "conflict", null)));
    }

}
