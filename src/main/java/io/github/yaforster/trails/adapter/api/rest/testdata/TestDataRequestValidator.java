package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataEntryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDefinitionDTO;
import io.github.yaforster.trails.core.ValidationViolation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates test-data transport input before it crosses into the domain.
 */
final class TestDataRequestValidator {

    void validateDefinition(TestDataSetDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateDefinition(request, violations);
        rejectIfInvalid(violations);
    }

    void validateUpdate(Long testDataId, TestDataSetDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateTestDataId(testDataId, violations);
        validateDefinition(request, violations);
        rejectIfInvalid(violations);
    }

    void validateTestDataId(Long testDataId) {
        List<ValidationViolation> violations = new ArrayList<>();
        validateTestDataId(testDataId, violations);
        rejectIfInvalid(violations);
    }

    void validatePage(Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (page == null) {
            violations.add(new ValidationViolation("TEST_DATA_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("TEST_DATA_PAGE_OUT_OF_RANGE",
                    "Page must be greater than or equal to 0.", "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("TEST_DATA_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("TEST_DATA_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    private void validateDefinition(TestDataSetDefinitionDTO request, List<ValidationViolation> violations) {
        if (request == null || request.getLabel() == null || request.getLabel()
                                                                    .isBlank()) {
            violations.add(
                    new ValidationViolation("TEST_DATA_LABEL_EMPTY", "Test data label must not be empty.", "/label"));
        }
        if (request == null || request.getValues() == null) {
            violations.add(
                    new ValidationViolation("TEST_DATA_VALUES_NULL", "Test data values must not be null.", "/values"));
            return;
        }
        validateEntries(request.getValues(), violations);
    }

    private void validateEntries(List<TestDataEntryDTO> entries, List<ValidationViolation> violations) {
        Set<String> keys = new HashSet<>();
        for (int index = 0; index < entries.size(); index++) {
            TestDataEntryDTO entry = entries.get(index);
            String path = "/values/" + index;
            if (entry == null) {
                violations.add(
                        new ValidationViolation("TEST_DATA_ENTRY_NULL", "Test data entry must not be null.", path));
                continue;
            }
            validateEntry(entry, path, keys, violations);
        }
    }

    private void validateEntry(TestDataEntryDTO entry, String path, Set<String> keys,
            List<ValidationViolation> violations) {
        String key = entry.getKey();
        if (key == null || key.isBlank()) {
            violations.add(
                    new ValidationViolation("TEST_DATA_KEY_EMPTY", "Test data key must not be empty.", path + "/key"));
        }
        if (entry.getValue() == null) {
            violations.add(new ValidationViolation("TEST_DATA_VALUE_NULL", "Test data value must not be null.",
                    path + "/value"));
        }
        if (key != null && !keys.add(key)) {
            violations.add(new ValidationViolation("TEST_DATA_KEY_DUPLICATE",
                    "Test data keys must be unique within a data set.", path + "/key"));
        }
    }

    private void validateTestDataId(Long testDataId, List<ValidationViolation> violations) {
        if (testDataId == null) {
            violations.add(
                    new ValidationViolation("TEST_DATA_ID_NULL", "Test data id must not be null.", "/testDataId"));
        }
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
