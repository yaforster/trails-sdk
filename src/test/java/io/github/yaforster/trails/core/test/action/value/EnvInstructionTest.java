package io.github.yaforster.trails.core.test.action.value;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EnvInstructionTest {

    private static final String ENV_VAR_NAME = "TEST_ENV_VAR";
    private static final String SYS_PROP_NAME = "TEST_SYS_PROP";

    @Test
    void shouldReturnEnvironmentVariableValueIfPresent() {
        String expectedValue = "envValue";
        System.setProperty(ENV_VAR_NAME, expectedValue);
        EnvInstruction instruction = new EnvInstruction(ENV_VAR_NAME, "default");
        String value = instruction.computeValue();

        Assertions.assertEquals(expectedValue, value);
        System.clearProperty(ENV_VAR_NAME);
    }

    @Test
    void shouldReturnSystemPropertyValueIfEnvIsNotPresent() {
        String expectedValue = "sysPropValue";
        System.clearProperty(SYS_PROP_NAME); // ensure clean state
        System.setProperty(SYS_PROP_NAME, expectedValue);
        EnvInstruction instruction = new EnvInstruction(SYS_PROP_NAME, "default");
        String value = instruction.computeValue();

        Assertions.assertEquals(expectedValue, value);
        System.clearProperty(SYS_PROP_NAME);
    }

    @Test
    void shouldReturnDefaultValueIfNeitherEnvNorSystemPropertyPresent() {
        String name = "NON_EXISTENT_VAR";
        String defaultValue = "fallback";
        EnvInstruction instruction = new EnvInstruction(name, defaultValue);
        String value = instruction.computeValue();

        Assertions.assertEquals(defaultValue, value);
    }

}
