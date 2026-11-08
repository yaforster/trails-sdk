package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.app.services.TestStepArrangementService;
import io.github.yaforster.trails.core.TrailsException;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.ActionGraphLimits;
import io.github.yaforster.trails.core.test.ValidatedActionGraph;
import io.github.yaforster.trails.core.test.TestCase;
import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestSet;
import io.github.yaforster.trails.core.test.TestStep;
import io.github.yaforster.trails.core.test.WebDriverData;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Builds executable browser-specific test sets from the closed Trails action graph. */
public final class TestStepArranger implements TestStepArrangementService {
    private final ActionGraphLimits limits;

    public TestStepArranger() {
        this(ActionGraphLimits.DEFAULTS);
    }

    public TestStepArranger(ActionGraphLimits limits) {
        this.limits = Objects.requireNonNull(limits, "Action graph limits are required");
    }

    @Override
    public List<TestSet> setUpTestsPerBrowser(TestExecutionData executionData) {
        try {
            TestStep startingStep = testStepChain(executionData.actions());
            return testSetsForBrowsers(startingStep, executionData);
        } catch (TestSetupException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new TestSetupException(exception);
        }
    }

    TestStep testStepChain(List<Action> actions) {
        Map<Long, Action> actionsById = indexActions(actions);
        Map<Long, List<Long>> successors = new LinkedHashMap<>();
        actionsById.forEach((id, action) -> successors.put(id,
                List.copyOf(Objects.requireNonNullElse(action.getNextActions(), List.of()))));
        ValidatedActionGraph graph = ValidatedActionGraph.validate(successors, limits);
        return assembleSteps(actionsById, graph);
    }

    private List<TestSet> testSetsForBrowsers(TestStep startingStep, TestExecutionData executionData) {
        return executionData.webDrivers()
                            .stream()
                            .map(webDriverData -> testSet(startingStep, executionData.testPlanLabel(), webDriverData))
                            .toList();
    }

    private TestSet testSet(TestStep startingStep, String testPlanLabel, WebDriverData webDriverData) {
        TestCase testCase = new TestCase(webDriverData.webAppURL(), startingStep);
        return new TestSet(testCase, webDriverData.webdriverProvider(), webDriverData.browserName(), testPlanLabel);
    }

    private Map<Long, Action> indexActions(List<Action> actions) {
        if (actions.size() > limits.maximumActions()) {
            throw new IllegalArgumentException("Action graph exceeds maximum actions: " + limits.maximumActions());
        }
        Map<Long, Action> actionsById = new LinkedHashMap<>();
        for (Action action : actions) {
            if (action == null || action.getActionID() == null) {
                throw new IllegalArgumentException("Action and identifier are required");
            }
            if (actionsById.putIfAbsent(action.getActionID(), action) != null) {
                throw new IllegalArgumentException("Duplicate action identifier: " + action.getActionID());
            }
        }
        return actionsById;
    }

    private TestStep assembleSteps(Map<Long, Action> actions, ValidatedActionGraph graph) {
        Map<Long, TestStep> stepsByActionId = new LinkedHashMap<>();
        for (Long id : graph.executionOrder()
                            .reversed()) {
            List<TestStep> successorSteps = graph.successors()
                                                 .get(id)
                                                 .stream()
                                                 .map(stepsByActionId::get)
                                                 .toList();
            stepsByActionId.put(id, new TestStep(actions.get(id), successorSteps));
        }
        return stepsByActionId.get(graph.executionOrder()
                                        .getFirst());
    }

    private static final class TestSetupException extends TrailsException {

        private TestSetupException(Throwable cause) {
            super("Could not prepare the test execution.", cause);
        }

    }

}
