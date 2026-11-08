package io.github.yaforster.trails.sdk.host.testdata;

import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.core.EntityMissingException;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import io.github.yaforster.trails.core.test.action.value.TestDataValueInstruction;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestDataValueResolverTest {

    @Test
    void shouldReplaceTestDataInstructionWithFixedValueInstruction() {
        TestDataDatabaseService testData = mock();
        when(testData.getValue(new TestDataDatabaseService.TestDataValue(4L, "username"))).thenReturn(
                Optional.of("alice"));

        ValueComputationInstruction result = new TestDataValueResolver(testData).resolveTestDataReference(
                new TestDataValueInstruction(4L, "username"));

        assertThat(result).isEqualTo(new FixedValueInstruction("alice"));
    }

    @Test
    void shouldRaiseEntityMissingException_whenReferencedValueDoesNotExist() {
        TestDataDatabaseService testData = mock();
        when(testData.getValue(new TestDataDatabaseService.TestDataValue(4L, "username"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new TestDataValueResolver(testData).resolveTestDataReference(
                new TestDataValueInstruction(4L, "username"))).isInstanceOf(EntityMissingException.class);
    }

    @Test
    void shouldKeepOriginalInstruction_whenInstructionDoesNotNeedResolution() {
        FixedValueInstruction instruction = new FixedValueInstruction("alice");

        ValueComputationInstruction result = new TestDataValueResolver(mock()).resolveTestDataReference(instruction);

        assertThat(result).isSameAs(instruction);
    }

}
