package io.github.yaforster.trails.core.test.action.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeStampInstructionTest {

    @Test
    void shouldFormatCurrentDateTimeWithGivenPattern() {
        String formatPattern = "yyyy-MM-dd HH:mm:ss";
        TimeStampInstruction instruction = new TimeStampInstruction(formatPattern);
        String result = instruction.computeValue();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
        Assertions.assertDoesNotThrow(() -> LocalDateTime.parse(result, formatter));
    }

    @Test
    void shouldThrowExceptionForInvalidPattern() {
        String invalidPattern = "invalid-pattern";
        TimeStampInstruction instruction = new TimeStampInstruction(invalidPattern);
        Assertions.assertThrows(IllegalArgumentException.class, instruction::computeValue);
    }

}
