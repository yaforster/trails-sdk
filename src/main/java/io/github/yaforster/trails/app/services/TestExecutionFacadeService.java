package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import io.github.yaforster.trails.core.notification.EmailRecipients;

import java.util.UUID;
import java.util.Objects;
import java.util.Optional;

@FunctionalInterface
public interface TestExecutionFacadeService {

    void executeTest(TestExecution testExecution);

    record TestExecution(UUID executionId, TestPlanRunDefinition runDefinition,
            Optional<EmailRecipients> emailRecipients) {

        public TestExecution {
            Objects.requireNonNull(executionId, "Execution identifier is required");
            Objects.requireNonNull(runDefinition, "Test run definition is required");
            Objects.requireNonNull(emailRecipients, "Email recipients are required");
        }

        public TestExecution(UUID executionId, TestPlanRunDefinition runDefinition) {
            this(executionId, runDefinition, Optional.empty());
        }

    }

}
