package io.github.yaforster.trails.adapter.api.rest.deletion;

import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.deletion.DeletionFailure;
import io.github.yaforster.trails.core.deletion.DeletionNotFound;
import io.github.yaforster.trails.core.deletion.DeletionSuccess;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpStatus;

/** Maps deletion outcomes consistently across contract resources. */
public final class DeletionResponseMapper {

    private DeletionResponseMapper() {}

    public static DatabaseDeletionResultDTO toDto(DatabaseDeletionResult result, Map<String, LinkDTO> notFoundLinks) {
        Objects.requireNonNull(result, "Database deletion result is required");
        Objects.requireNonNull(notFoundLinks, "Not-found links are required");
        return switch (result) {
            case DeletionSuccess success ->
                response(success.id(), true, "Database entry was successfully deleted.", Map.of());
            case DeletionNotFound missing ->
                response(missing.id(), false, "No database entry with the given values was found.", notFoundLinks);
            case DeletionFailure failure -> response(failure.id(), false, failure.message(), Map.of());
        };
    }

    public static HttpStatus status(DatabaseDeletionResult result) {
        Objects.requireNonNull(result, "Database deletion result is required");
        return switch (result) {
            case DeletionFailure failure when failure.error() == null -> HttpStatus.CONFLICT;
            case DeletionFailure _ -> HttpStatus.INTERNAL_SERVER_ERROR;
            case DeletionNotFound _ -> HttpStatus.NOT_FOUND;
            case DeletionSuccess _ -> HttpStatus.OK;
        };
    }

    private static DatabaseDeletionResultDTO response(Long id, boolean deletionResult, String message,
            Map<String, LinkDTO> links) {
        return new DatabaseDeletionResultDTO().deletionRequestedForID(id)
                                              .deletionResult(deletionResult)
                                              .message(message)
                                              .links(Map.copyOf(links));
    }

}
