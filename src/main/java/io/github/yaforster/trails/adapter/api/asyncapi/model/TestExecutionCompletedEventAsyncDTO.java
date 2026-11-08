package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.Map;

/**
 * Server-sent event emitted after a test execution persists a completed result.
 *
 * @param executionId
 *            execution correlation identifier
 * @param status
 *            completed lifecycle status
 * @param message
 *            safe lifecycle summary
 * @param testRunId
 *            persisted test-run identifier
 * @param links
 *            public event and result links
 */
public record TestExecutionCompletedEventAsyncDTO(String executionId, Status status, String message, Long testRunId,
        Map<String, LinkAsyncDTO> links) {

    public enum Status {

        COMPLETED

    }

}
