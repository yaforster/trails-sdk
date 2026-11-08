package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.ValidationViolation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestRunRequestValidatorTest {

    @Test
    void shouldReportEveryMissingTestRunHierarchyIdentifier() {
        TestRunRequestValidator validator = new TestRunRequestValidator();

        assertThatThrownBy(() -> validator.validateTestRun(null, null, null)).isInstanceOfSatisfying(
                ApiRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(ValidationViolation::code)
                                                               .containsExactly("TEST_RUN_APPLICATION_ID_NULL",
                                                                       "TEST_RUN_STAGE_ID_NULL", "TEST_RUN_ID_NULL"));
    }

    @Test
    void shouldRejectOutOfRangeHistoryPaging() {
        TestRunRequestValidator validator = new TestRunRequestValidator();

        assertThatThrownBy(() -> validator.validateHistory(1L, 2L, 3L, -1, 101)).isInstanceOfSatisfying(
                ApiRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(ValidationViolation::code)
                                                               .containsExactly("TEST_RUN_HISTORY_PAGE_OUT_OF_RANGE",
                                                                       "TEST_RUN_HISTORY_SIZE_OUT_OF_RANGE"));
    }

    @Test
    void shouldAcceptCompleteTestRunReferences() {
        TestRunRequestValidator validator = new TestRunRequestValidator();

        assertThatCode(() -> validator.validateHistory(1L, 2L, 3L, 0, 20)).doesNotThrowAnyException();
    }

}
