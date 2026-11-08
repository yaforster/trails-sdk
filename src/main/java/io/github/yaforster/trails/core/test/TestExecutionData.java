package io.github.yaforster.trails.core.test;

import java.util.List;

public record TestExecutionData(List<WebDriverData> webDrivers, List<Action> actions, Long applicationID, Long stageID,
        Long testPlanID, String testPlanLabel) {
    public TestExecutionData {
        webDrivers = List.copyOf(webDrivers);
        actions = List.copyOf(actions);
    }

}
