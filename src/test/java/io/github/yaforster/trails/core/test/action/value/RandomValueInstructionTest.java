package io.github.yaforster.trails.core.test.action.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class RandomValueInstructionTest {

    @Test
    void givenNullPrefixSuffixAndValidPool_shouldGenerateStringOfCorrectLength() {
        var instruction = new RandomValueInstruction(null, null, "ABCDEF", 10);
        String result = instruction.computeValue();
        Assertions.assertEquals(10, result.length());
    }

    @Test
    void givenEmptyCharPool_shouldReturnPrefixAndSuffixOnly() {
        var instruction = new RandomValueInstruction("PRE-", "-SUF", "", 8);
        String result = instruction.computeValue();
        Assertions.assertEquals("PRE--SUF", result);
    }

    @Test
    void givenNullCharPool_shouldReturnPrefixAndSuffixOnly() {
        var instruction = new RandomValueInstruction("PRE-", "-SUF", null, 8);
        String result = instruction.computeValue();
        Assertions.assertEquals("PRE--SUF", result);
    }

    @Test
    void givenZeroRandomLength_shouldReturnPrefixAndSuffixOnly() {
        var instruction = new RandomValueInstruction("PRE-", "-SUF", "ABC", 0);
        String result = instruction.computeValue();
        Assertions.assertEquals("PRE--SUF", result);
    }

    @Test
    void givenNegativeRandomLength_shouldReturnPrefixAndSuffixOnly() {
        var instruction = new RandomValueInstruction("PRE-", "-SUF", "ABC", -5);
        String result = instruction.computeValue();
        Assertions.assertEquals("PRE--SUF", result);
    }

    @Test
    void givenPrefix_shouldStartWithPrefix() {
        var instruction = new RandomValueInstruction("START-", "-END", "XYZ", 5);
        String result = instruction.computeValue();
        Assertions.assertTrue(result.startsWith("START-"));
    }

    @Test
    void givenSuffix_shouldEndWithSuffix() {
        var instruction = new RandomValueInstruction("START-", "-END", "XYZ", 5);
        String result = instruction.computeValue();
        Assertions.assertTrue(result.endsWith("-END"));
    }

    @Test
    void givenNullPrefix_shouldStartWithRandomPart() {
        var instruction = new RandomValueInstruction(null, "-XYZ", "123", 4);
        String result = instruction.computeValue();
        Assertions.assertFalse(result.startsWith("null"));
    }

    @Test
    void givenNullSuffix_shouldEndWithRandomPart() {
        var instruction = new RandomValueInstruction("PRE-", null, "123", 4);
        String result = instruction.computeValue();
        Assertions.assertFalse(result.endsWith("null"));
    }

    @Test
    void givenCharPool_shouldOnlyUseCharsFromPool() {
        var charPool = "ABC123";
        var instruction = new RandomValueInstruction("", "", charPool, 20);
        String result = instruction.computeValue();
        Assertions.assertTrue(result.chars()
                                    .allMatch(c -> charPool.indexOf(c) >= 0));
    }

    @Test
    void givenValidInput_shouldHaveCorrectTotalLength() {
        var prefix = "PRE-";
        var suffix = "-SUF";
        int randomLength = 12;
        var instruction = new RandomValueInstruction(prefix, suffix, "XYZ", randomLength);
        String result = instruction.computeValue();
        Assertions.assertEquals(prefix.length() + randomLength + suffix.length(), result.length());
    }

    @Test
    void givenAllNullsAndZeroLength_shouldReturnEmptyString() {
        var instruction = new RandomValueInstruction(null, null, null, 0);
        String result = instruction.computeValue();
        Assertions.assertEquals("", result);
    }

    @Test
    void givenLongPrefixAndSuffix_shouldBePreservedExactly() {
        var prefix = "<<<LONG_PREFIX>>>";
        var suffix = "<<<LONG_SUFFIX>>>";
        var instruction = new RandomValueInstruction(prefix, suffix, "01", 5);
        String result = instruction.computeValue();
        Assertions.assertTrue(result.startsWith(prefix));
        Assertions.assertTrue(result.endsWith(suffix));
    }

}
