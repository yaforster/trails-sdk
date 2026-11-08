package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;

import java.util.Optional;

public interface TestDataDatabaseService {

    record TestDataUpdate(Long testDataId, TestDataSetDefinition definition) {
    }

    record TestDataDetails(Long testDataId, boolean includeRetired) {
    }

    record TestDataPage(int page, int size, boolean includeRetired) {
    }

    record TestDataValue(Long testDataId, String key) {
    }

    TestDataSet storeTestData(TestDataSetDefinition definition);

    Optional<TestDataSet> updateTestData(TestDataUpdate testDataUpdate);

    Optional<TestDataSet> getTestData(Long id);

    Optional<TestDataSet> getTestData(TestDataDetails testDataDetails);

    PagedResult<TestDataSet> listTestData(TestDataPage testDataPage);

    DatabaseDeletionResult deleteTestData(Long id);

    DatabaseDeletionResult restoreTestData(Long id);

    Optional<String> getValue(TestDataValue testDataValue);

}
