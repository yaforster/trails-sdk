package io.github.yaforster.trails.core.test.action.value;

import java.time.LocalDate;
import java.time.Clock;
import java.time.format.DateTimeFormatter;

public record RelativeDateInstruction(int offsetDays, String formatPattern) implements ValueComputationInstruction {

    @Override
    public String computeValue() {
        return computeValue(Clock.systemDefaultZone());
    }

    @Override
    public String computeValue(Clock clock) {
        return LocalDate.now(clock)
                        .plusDays(offsetDays)
                        .format(DateTimeFormatter.ofPattern(formatPattern));
    }
}
