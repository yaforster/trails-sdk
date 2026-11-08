package io.github.yaforster.trails.core.test.action.value;

import java.time.Clock;

public sealed interface ValueComputationInstruction permits EnvInstruction, FixedValueInstruction,
        RandomValueInstruction, RelativeDateInstruction, TestDataValueInstruction, TimeStampInstruction {

    String computeValue();

    default String computeValue(Clock clock) {
        return computeValue();
    }

}
