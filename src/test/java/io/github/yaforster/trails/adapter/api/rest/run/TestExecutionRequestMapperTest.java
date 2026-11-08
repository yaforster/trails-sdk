package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunCompletionNotificationDTO;
import io.github.yaforster.trails.core.test.Browser;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TestExecutionRequestMapperTest {

    private final TestExecutionRequestMapper mapper = new TestExecutionRequestMapper();

    @Test
    void shouldMapValidatedDynamicRecipients_whenEmailNotificationIsEnabled() {
        TestExecutionRequestMapper.MappedRequest mapped = mapper.map(request().completionNotification(
                new TestRunCompletionNotificationDTO().emailEnabled(true)
                                                      .recipientEmails(
                                                              List.of("Alice@EXAMPLE.test", "bob@example.test"))));

        assertThat(mapped.runDefinition()
                         .browsersToTest()).containsExactly(Browser.CHROME);
        assertThat(mapped.emailRecipients()).hasValueSatisfying(
                recipients -> assertThat(recipients.values()).containsExactly("Alice@example.test",
                        "bob@example.test"));
    }

    @Test
    void shouldIgnoreRecipients_whenEmailNotificationIsDisabled() {
        TestExecutionRequestMapper.MappedRequest mapped = mapper.map(request().completionNotification(
                new TestRunCompletionNotificationDTO().emailEnabled(false)
                                                      .recipientEmails(Collections.singletonList(null))));

        assertThat(mapped.emailRecipients()).isEmpty();
    }

    @Test
    void shouldRejectDuplicateNormalizedRecipients_beforeExecution() {
        TestPlanRunDefinitionDTO request = request().completionNotification(
                new TestRunCompletionNotificationDTO().emailEnabled(true)
                                                      .recipientEmails(
                                                              List.of("Alice@EXAMPLE.test", "Alice@example.test")));

        assertThatThrownBy(() -> mapper.map(request)).isInstanceOfSatisfying(
                TestExecutionRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                               .containsExactly(
                                                                       "COMPLETION_NOTIFICATION_RECIPIENT_DUPLICATE"));
    }

    @Test
    void shouldRejectNullRecipientAsInvalid_whenEmailNotificationIsEnabled() {
        TestPlanRunDefinitionDTO request = request().completionNotification(
                new TestRunCompletionNotificationDTO().emailEnabled(true)
                                                      .recipientEmails(Collections.singletonList(null)));

        assertThatThrownBy(() -> mapper.map(request)).isInstanceOfSatisfying(
                TestExecutionRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                               .containsExactly(
                                                                       "COMPLETION_NOTIFICATION_RECIPIENT_INVALID"));
    }

    @Test
    void shouldRejectSurroundingControlCharacter_beforeTrimmingRecipient() {
        TestPlanRunDefinitionDTO request = request().completionNotification(
                new TestRunCompletionNotificationDTO().emailEnabled(true)
                                                      .recipientEmails(List.of("\nAlice@example.test")));

        assertThatThrownBy(() -> mapper.map(request)).isInstanceOfSatisfying(
                TestExecutionRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                               .containsExactly(
                                                                       "COMPLETION_NOTIFICATION_RECIPIENT_INVALID"));
    }

    @Test
    void shouldRejectExplicitNullEmailEnabled() {
        TestPlanRunDefinitionDTO request = request().completionNotification(
                new TestRunCompletionNotificationDTO().emailEnabled(null));

        assertThatThrownBy(() -> mapper.map(request)).isInstanceOfSatisfying(
                TestExecutionRequestValidationException.class,
                exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                               .containsExactly(
                                                                       "COMPLETION_NOTIFICATION_EMAIL_ENABLED_NULL"));
    }

    @Test
    void shouldRejectMissingTestPlanBeforeExecution() {
        assertThatThrownBy(() -> mapper.map(
                new TestPlanRunDefinitionDTO().browsersToTest(List.of(BrowserDTO.CHROME)))).isInstanceOfSatisfying(
                        TestExecutionRequestValidationException.class,
                        exception -> assertThat(exception.violations()).extracting(violation -> violation.code())
                                                                       .containsExactly(
                                                                               "TEST_EXECUTION_TEST_PLAN_ID_NULL"));
    }

    private TestPlanRunDefinitionDTO request() {
        return new TestPlanRunDefinitionDTO().testPlanID(42L)
                                             .browsersToTest(List.of(BrowserDTO.CHROME));
    }

}
