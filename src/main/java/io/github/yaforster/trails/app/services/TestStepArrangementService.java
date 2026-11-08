package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.TestExecutionData;
import io.github.yaforster.trails.core.test.TestSet;

import java.util.List;

@FunctionalInterface
public interface TestStepArrangementService {

    List<TestSet> setUpTestsPerBrowser(TestExecutionData testExecutionData);

}
