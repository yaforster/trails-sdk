package io.github.yaforster.trails.sdk.notification;
import io.github.yaforster.trails.core.notification.EmailRecipients;

import io.github.yaforster.trails.core.data.TestRunTerminalStatus;
import io.github.yaforster.trails.core.data.ResultIndicator;
import java.net.URI;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Immutable data admitted to a notifier after a terminal test result is persisted.
 *
 * @param executionId
 *            execution correlation identifier
 * @param testRunId
 *            persisted test-run identifier
 * @param status
 *            terminal host status
 * @param resultIndicator
 *            persisted aggregate test outcome, independent of host completion
 * @param resultUri
 *            optional public result URI
 * @param emailRecipients
 *            optional request-scoped recipients
 */
public record TerminalRunNotification(UUID executionId, long testRunId, TestRunTerminalStatus status,
        ResultIndicator resultIndicator, Optional<URI> resultUri, Optional<EmailRecipients> emailRecipients) {

    public TerminalRunNotification {
        Objects.requireNonNull(executionId, "Execution identifier is required");
        if (testRunId <= 0) {
            throw new IllegalArgumentException("A persisted test run identifier is required");
        }
        Objects.requireNonNull(status, "Terminal run status is required");
        Objects.requireNonNull(resultIndicator, "Test result indicator is required");
        Objects.requireNonNull(resultUri, "Result URI is required");
        Objects.requireNonNull(emailRecipients, "Email recipients are required");
    }

}
