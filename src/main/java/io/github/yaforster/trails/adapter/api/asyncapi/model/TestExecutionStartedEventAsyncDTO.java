package io.github.yaforster.trails.adapter.api.asyncapi.model;

import java.util.Map;

/**
 * Server-sent event emitted after an accepted test execution begins.
 *
 * @param executionId
 *            execution correlation identifier
 * @param status
 *            running lifecycle status
 * @param message
 *            safe lifecycle summary
 * @param links
 *            public event links
 */
public record TestExecutionStartedEventAsyncDTO(String executionId, Status status, String message,
        Map<String, LinkAsyncDTO> links) {

    public enum Status {

        RUNNING

    }

}
