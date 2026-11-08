package io.github.yaforster.trails.app;

import io.github.yaforster.trails.app.services.TestRunTerminalOutcome;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

public interface TestExecutionEventPublisher {

    void publishStarted(UUID executionId);

    Optional<URI> publishTerminal(UUID executionId, TestRunTerminalOutcome outcome);

    void publishUnpersistedFailure(UUID executionId);

}
