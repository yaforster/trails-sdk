package io.github.yaforster.trails.sdk.host.execution;

import io.github.yaforster.trails.core.data.TestPlan;
import io.github.yaforster.trails.core.test.TestExecutionData;

import java.util.Objects;

record ExecutionTarget(Long applicationId, Long stageId, Long testPlanId, String label) {

    static ExecutionTarget from(TestPlan testPlan) {
        return new ExecutionTarget(testPlan.getApplicationID(), testPlan.getStageID(), testPlan.getId(),
                testPlan.getLabel());
    }

    void requireMatching(TestExecutionData executionData) {
        if (!Objects.equals(applicationId, executionData.applicationID())
                || !Objects.equals(stageId, executionData.stageID())
                || !Objects.equals(testPlanId, executionData.testPlanID())
                || !Objects.equals(label, executionData.testPlanLabel())) {
            throw new IllegalStateException("Browser execution data does not match the accepted test plan");
        }
    }

}
