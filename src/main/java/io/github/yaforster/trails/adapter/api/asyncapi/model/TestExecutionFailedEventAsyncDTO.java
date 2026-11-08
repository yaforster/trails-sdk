package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.Map;

/**
 * Server-sent event emitted after an execution failure.
 *
 * @param executionId
 *            execution correlation identifier
 * @param status
 *            failed lifecycle status
 * @param message
 *            safe lifecycle summary
 * @param error
 *            safe structured error, when present
 * @param links
 *            public event link and, for persisted failures, result link
 */
public record TestExecutionFailedEventAsyncDTO(String executionId, Status status, String message, Object error,
        Map<String, LinkAsyncDTO> links) {

    public enum Status {

        FAILED

    }

}
