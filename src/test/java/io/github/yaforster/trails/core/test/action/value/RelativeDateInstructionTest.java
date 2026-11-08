package io.github.yaforster.trails.core.test.action.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

class RelativeDateInstructionTest {

    @Test
    void givenZeroOffsetAndValidFormat_shouldReturnTodayFormatted() {
        var format = "yyyy-MM-dd";
        var instruction = new RelativeDateInstruction(0, format);
        String result = instruction.computeValue();
        String expected = LocalDate.now()
                                   .format(DateTimeFormatter.ofPattern(format));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void givenPositiveOffset_shouldReturnFutureDateFormatted() {
        var format = "yyyy/MM/dd";
        int offset = 5;
        var instruction = new RelativeDateInstruction(offset, format);
        String result = instruction.computeValue();
        String expected = LocalDate.now()
                                   .plusDays(offset)
                                   .format(DateTimeFormatter.ofPattern(format));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void givenNegativeOffset_shouldReturnPastDateFormatted() {
        var format = "dd-MM-yyyy";
        int offset = -7;
        var instruction = new RelativeDateInstruction(offset, format);
        String result = instruction.computeValue();
        String expected = LocalDate.now()
                                   .minusDays(7)
                                   .format(DateTimeFormatter.ofPattern(format));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void givenCustomFormat_shouldMatchExpectedPattern() {
        var format = "EEEE, MMMM d, yyyy";
        var instruction = new RelativeDateInstruction(0, format);
        String result = instruction.computeValue();
        Assertions.assertNotNull(result);
    }

    @Test
    void givenInvalidFormat_shouldThrowDateTimeParseException() {
        var format = "invalid-format";
        var instruction = new RelativeDateInstruction(0, format);
        Assertions.assertThrows(IllegalArgumentException.class, instruction::computeValue);
    }

    @Test
    void givenNullFormat_shouldThrowNullPointerException() {
        var instruction = new RelativeDateInstruction(0, null);
        Assertions.assertThrows(NullPointerException.class, instruction::computeValue);
    }

    @Test
    void givenOffsetOnly_shouldAffectOnlyTheDateValue() {
        int offset = 10;
        var format = "yyyy-MM-dd";
        var instruction = new RelativeDateInstruction(offset, format);
        String result = instruction.computeValue();
        LocalDate computedDate = LocalDate.parse(result, DateTimeFormatter.ofPattern(format));
        Assertions.assertEquals(LocalDate.now()
                                         .plusDays(offset),
                computedDate);
    }

}
