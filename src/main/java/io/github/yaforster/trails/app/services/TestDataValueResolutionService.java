package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;

@FunctionalInterface
public interface TestDataValueResolutionService {

    ValueComputationInstruction resolveTestDataReference(ValueComputationInstruction instruction);

}
