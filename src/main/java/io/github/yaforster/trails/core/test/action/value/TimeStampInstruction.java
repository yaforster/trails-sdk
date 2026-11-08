package io.github.yaforster.trails.core.test.action.value;

import java.time.LocalDateTime;
import java.time.Clock;
import java.time.format.DateTimeFormatter;

public record TimeStampInstruction(String formatPattern) implements ValueComputationInstruction {

    @Override
    public String computeValue() {
        return computeValue(Clock.systemDefaultZone());
    }

    @Override
    public String computeValue(Clock clock) {
        LocalDateTime dateTime = LocalDateTime.now(clock);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        return dateTime.format(formatter);
    }
}
