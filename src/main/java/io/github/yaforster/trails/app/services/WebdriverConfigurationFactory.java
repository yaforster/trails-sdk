package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;

@FunctionalInterface
public interface WebdriverConfigurationFactory {

    TestExecutionData getTestExecutionData(TestPlanRunDefinition runDefinition);

}
