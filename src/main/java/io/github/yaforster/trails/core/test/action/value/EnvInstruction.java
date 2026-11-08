package io.github.yaforster.trails.core.test.action.value;

import java.util.Optional;

public record EnvInstruction(String variableName, String defaultValue) implements ValueComputationInstruction {

    @Override
    public String computeValue() {
        return Optional.ofNullable(System.getenv(variableName))
                       .or(() -> Optional.ofNullable(System.getProperty(variableName)))
                       .orElse(defaultValue);
    }
}
