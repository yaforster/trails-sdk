package io.github.yaforster.trails.core.test.action.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class FixedValueInstructionTest {

    @Test
    void givenRegularString_shouldReturnSameString() {
        var input = "hello-world";
        var instruction = new FixedValueInstruction(input);
        String result = instruction.computeValue();
        Assertions.assertEquals(input, result);
    }

    @Test
    void givenEmptyString_shouldReturnEmptyString() {
        var instruction = new FixedValueInstruction("");
        String result = instruction.computeValue();
        Assertions.assertEquals("", result);
    }

    @Test
    void givenWhitespaceString_shouldReturnSameWhitespace() {
        var instruction = new FixedValueInstruction("   ");
        String result = instruction.computeValue();
        Assertions.assertEquals("   ", result);
    }

    @Test
    void givenUnicodeString_shouldReturnSameUnicodeString() {
        var input = "ÃƒÂ¤Ã‚Â½Ã‚Â ÃƒÂ¥Ã‚Â¥Ã‚Â½ÃƒÂ¤Ã‚Â¸Ã¢â‚¬â€œÃƒÂ§Ã¢â‚¬Â¢Ã…â€™ÃƒÂ°Ã…Â¸Ã…â€™Ã‚Â";
        var instruction = new FixedValueInstruction(input);
        String result = instruction.computeValue();
        Assertions.assertEquals(input, result);
    }

    @Test
    void givenNull_shouldReturnNull() {
        var instruction = new FixedValueInstruction(null);
        String result = instruction.computeValue();
        Assertions.assertNull(result);
    }

}
