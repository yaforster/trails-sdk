package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.TestPlanRunDefinition;

import java.util.Objects;
import java.util.function.Consumer;

@FunctionalInterface
public interface TestService {

    void runTest(TestRunExecution testRunExecution);

    record TestRunExecution(TestPlanRunDefinition runDefinition, TestRunLifecycle lifecycle) {

        public TestRunExecution {
            Objects.requireNonNull(runDefinition);
            Objects.requireNonNull(lifecycle);
        }

    }

    /**
     * Receives lifecycle transitions; terminal results include both completed and safely persisted failed runs.
     *
     * @param onStart
     *            invoked when the admitted run begins
     * @param onTerminal
     *            invoked after a result is persisted, with its transient terminal status
     * @param onUnpersistedFailure
     *            invoked only when no terminal result could be persisted
     */
    record TestRunLifecycle(Runnable onStart, Consumer<TestRunTerminalOutcome> onTerminal,
            Consumer<Exception> onUnpersistedFailure) {

        public TestRunLifecycle {
            Objects.requireNonNull(onStart);
            Objects.requireNonNull(onTerminal);
            Objects.requireNonNull(onUnpersistedFailure);
        }

    }

}
