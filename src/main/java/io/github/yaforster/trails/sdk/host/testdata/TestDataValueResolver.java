package io.github.yaforster.trails.sdk.host.testdata;

import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.app.services.TestDataValueResolutionService;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.action.value.TestDataValueInstruction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

import java.util.Objects;

/**
 * Resolves a test-data reference before a closed Trails value action executes.
 */
public final class TestDataValueResolver implements TestDataValueResolutionService {

    private final TestDataDatabaseService testData;

    public TestDataValueResolver(TestDataDatabaseService testData) {
        this.testData = Objects.requireNonNull(testData, "Test data persistence is required");
    }

    @Override
    public ValueComputationInstruction resolveTestDataReference(ValueComputationInstruction instruction) {
        if (!(instruction instanceof TestDataValueInstruction(Long testDataId, String key))) {
            return instruction;
        }
        String value = testData.getValue(new TestDataDatabaseService.TestDataValue(testDataId, key))
                               .orElseThrow(() -> new EntityMissingException("Test data value with data set id "
                                       + testDataId + " and key " + key + " could not be found."));
        return new FixedValueInstruction(value);
    }

}
