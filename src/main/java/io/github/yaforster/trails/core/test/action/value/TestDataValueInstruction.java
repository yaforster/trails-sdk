package io.github.yaforster.trails.core.test.action.value;

public record TestDataValueInstruction(Long testDataId, String key) implements ValueComputationInstruction {

    @Override
    public String computeValue() {
        throw new IllegalStateException("Test data values must be resolved before action execution.");
    }
}
