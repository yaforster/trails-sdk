package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunCompletionNotificationDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.TestPlanRunDefinition;
import io.github.yaforster.trails.core.notification.EmailRecipients;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validates and maps the REST execution request before it can be admitted to the host.
 */
public final class TestExecutionRequestMapper {

    public MappedRequest map(TestPlanRunDefinitionDTO request) {
        List<ValidationViolation> violations = validateRequiredExecutionFields(request);
        Optional<EmailRecipients> recipients = mapRecipients(request, violations);
        if (!violations.isEmpty()) {
            throw new TestExecutionRequestValidationException(violations);
        }
        return new MappedRequest(new TestPlanRunDefinition(request.getTestPlanID(), mapBrowsers(request)), recipients);
    }

    private List<ValidationViolation> validateRequiredExecutionFields(TestPlanRunDefinitionDTO request) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (request == null) {
            violations.add(
                    violation("TEST_EXECUTION_DEFINITION_NULL", "Test execution definition must not be null.", "/"));
            return violations;
        }
        if (request.getTestPlanID() == null) {
            violations.add(
                    violation("TEST_EXECUTION_TEST_PLAN_ID_NULL", "Test plan id must not be null.", "/testPlanID"));
        }
        if (request.getBrowsersToTest() == null) {
            violations.add(violation("TEST_EXECUTION_BROWSERS_TO_TEST_NULL", "Browsers to test must not be null.",
                    "/browsersToTest"));
        } else if (request.getBrowsersToTest()
                          .stream()
                          .anyMatch(java.util.Objects::isNull)) {
            violations.add(violation("TEST_EXECUTION_BROWSER_INVALID", "Browser must not be null.", "/browsersToTest"));
        }
        return violations;
    }

    private Optional<EmailRecipients> mapRecipients(TestPlanRunDefinitionDTO request,
            List<ValidationViolation> violations) {
        if (request == null || request.getCompletionNotification() == null) {
            return Optional.empty();
        }
        TestRunCompletionNotificationDTO notification = request.getCompletionNotification();
        if (notification.getEmailEnabled() == null) {
            violations.add(violation("COMPLETION_NOTIFICATION_EMAIL_ENABLED_NULL",
                    "Completion notification emailEnabled must not be null.", "/completionNotification/emailEnabled"));
            return Optional.empty();
        }
        if (!notification.getEmailEnabled()) {
            return Optional.empty();
        }
        List<String> recipientEmails = notification.getRecipientEmails();
        if (recipientEmails == null || recipientEmails.isEmpty()
                || recipientEmails.size() > EmailRecipients.MAXIMUM_RECIPIENTS) {
            violations.add(violation("COMPLETION_NOTIFICATION_RECIPIENT_COUNT",
                    "Completion notification requires between one and 100 recipient emails.",
                    "/completionNotification/recipientEmails"));
            return Optional.empty();
        }
        try {
            return Optional.of(new EmailRecipients(recipientEmails));
        } catch (IllegalArgumentException exception) {
            String code = "COMPLETION_NOTIFICATION_RECIPIENT_INVALID";
            if (exception instanceof io.github.yaforster.trails.core.notification.DuplicateEmailRecipientException) {
                code = "COMPLETION_NOTIFICATION_RECIPIENT_DUPLICATE";
            }
            violations.add(violation(code, "Completion notification contains an invalid recipient email.",
                    "/completionNotification/recipientEmails"));
            return Optional.empty();
        }
    }

    private List<Browser> mapBrowsers(TestPlanRunDefinitionDTO request) {
        return request.getBrowsersToTest()
                      .stream()
                      .map(this::mapBrowser)
                      .toList();
    }

    private Browser mapBrowser(BrowserDTO browser) {
        return Browser.valueOf(browser.name());
    }

    private ValidationViolation violation(String code, String message, String path) {
        return new ValidationViolation(code, message, path);
    }

    /**
     * Validated execution data ready for host admission.
     *
     * @param runDefinition
     *            executable test-plan request
     * @param emailRecipients
     *            optional request-scoped notification recipients
     */
    public record MappedRequest(TestPlanRunDefinition runDefinition, Optional<EmailRecipients> emailRecipients) {

    }

}
