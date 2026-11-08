package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataEntryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDefinitionDTO;
import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestDataRequestValidatorTest extends TrailsTest {

    private final TestDataRequestValidator validator = new TestDataRequestValidator();

    @Test
    void shouldReportAllInvalidDefinitionFieldsInStableOrder() {
        TestDataSetDefinitionDTO request = new TestDataSetDefinitionDTO(" ", List.of(new TestDataEntryDTO().key(" ")
                                                                                                           .value(null),
                new TestDataEntryDTO().key("duplicate")
                                      .value("one"),
                new TestDataEntryDTO().key("duplicate")
                                      .value("two")));

        assertThatThrownBy(() -> validator.validateDefinition(request)).isInstanceOfSatisfying(
                ApiRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                               .containsExactly("TEST_DATA_LABEL_EMPTY",
                                                                       "TEST_DATA_KEY_EMPTY", "TEST_DATA_VALUE_NULL",
                                                                       "TEST_DATA_KEY_DUPLICATE"));
    }

    @Test
    void shouldRejectNullEntriesBeforeMappingThemToDomainValues() {
        TestDataSetDefinitionDTO request = new TestDataSetDefinitionDTO("Credentials", List.of());
        request.setValues(Arrays.asList((TestDataEntryDTO) null));

        assertThatThrownBy(
                () -> validator.validateDefinition(request)).isInstanceOfSatisfying(ApiRequestValidationException.class,
                        exception -> assertThat(exception.violations()).singleElement()
                                                                       .satisfies(violation -> {
                                                                           assertThat(violation.code()).isEqualTo(
                                                                                   "TEST_DATA_ENTRY_NULL");
                                                                           assertThat(violation.path()).isEqualTo(
                                                                                   "/values/0");
                                                                       }));
    }

    @Test
    void shouldRejectAnInvalidPageBeforeInvokingPersistence() {
        assertThatThrownBy(() -> validator.validatePage(-1, 101)).isInstanceOfSatisfying(
                ApiRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                               .containsExactly("TEST_DATA_PAGE_OUT_OF_RANGE",
                                                                       "TEST_DATA_SIZE_OUT_OF_RANGE"));
    }

}
