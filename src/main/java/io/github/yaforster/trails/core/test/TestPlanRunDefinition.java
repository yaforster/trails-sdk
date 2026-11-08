package io.github.yaforster.trails.core.test;

import java.util.List;
import java.util.Objects;

public record TestPlanRunDefinition(Long testPlanID, List<Browser> browsersToTest) {

    public TestPlanRunDefinition {
        Objects.requireNonNull(testPlanID, "Test plan identifier is required");
        browsersToTest = List.copyOf(Objects.requireNonNull(browsersToTest, "Browsers to test are required"));
    }

}
