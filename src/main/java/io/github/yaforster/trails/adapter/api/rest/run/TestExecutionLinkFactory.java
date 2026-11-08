package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.asyncapi.model.LinkAsyncDTO;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Creates stable, contract-defined links for execution responses and events. */
public final class TestExecutionLinkFactory {

    public URI eventUri(UUID executionId) {
        return URI.create("/api/test/events/" + requireExecutionId(executionId));
    }

    public URI resultUri(PersistedTestRunResult result) {
        Objects.requireNonNull(result, "Persisted test run result is required");
        return URI.create("/api/testruns/" + requireIdentifier(result.applicationId(), "application") + "/"
                + requireIdentifier(result.stageId(), "stage") + "/" + requireIdentifier(result.id(), "test run"));
    }

    public Map<String, LinkAsyncDTO> startedLinks(UUID executionId) {
        return Map.of("self", link(eventUri(executionId)));
    }

    public TerminalLinks terminalLinks(UUID executionId, PersistedTestRunResult result) {
        URI resultUri = resultUri(result);
        return new TerminalLinks(Map.of("self", link(eventUri(executionId)), "result", link(resultUri)), resultUri);
    }

    public Map<String, LinkAsyncDTO> unpersistedFailureLinks(UUID executionId) {
        return Map.of("self", link(eventUri(executionId)));
    }

    private LinkAsyncDTO link(URI uri) {
        return new LinkAsyncDTO(uri.toString());
    }

    private String requireExecutionId(UUID executionId) {
        return Objects.requireNonNull(executionId, "Execution identifier is required")
                      .toString();
    }

    private String requireIdentifier(Long identifier, String label) {
        if (identifier == null || identifier <= 0) {
            throw new IllegalArgumentException("Persisted " + label + " identifier is required");
        }
        return identifier.toString();
    }

    /**
     * Links and URI published for a persisted terminal result.
     *
     * @param links
     *            event payload links
     * @param resultUri
     *            public persisted result URI
     */
    public record TerminalLinks(Map<String, LinkAsyncDTO> links, URI resultUri) {

        public TerminalLinks {
            links = Map.copyOf(Objects.requireNonNull(links, "Terminal links are required"));
            Objects.requireNonNull(resultUri, "Result URI is required");
        }

    }

}
