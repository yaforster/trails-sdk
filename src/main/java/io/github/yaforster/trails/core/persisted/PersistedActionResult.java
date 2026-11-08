package io.github.yaforster.trails.core.persisted;

public record PersistedActionResult(Long id, Long testPathResultId, Long actionId, String label, String message,
        ActionResultType resultType, String exceptionMessageFromAction) {

}
