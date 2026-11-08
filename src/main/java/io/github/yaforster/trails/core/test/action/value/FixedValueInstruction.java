package io.github.yaforster.trails.core.test.action.value;

public record FixedValueInstruction(String value) implements ValueComputationInstruction {

    @Override
    public String computeValue() {
        return value;
    }
}
