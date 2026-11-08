package io.github.yaforster.trails.sdk.notification;
import io.github.yaforster.trails.core.notification.EmailRecipients;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class EmailRecipientsTest {

    @Test
    void shouldNormalizeDomainWhilePreservingLocalPartCase() {
        EmailRecipients recipients = new EmailRecipients(List.of(" Recipient@Example.com ", "recipient@example.com"));

        assertThat(recipients.values()).containsExactly("Recipient@example.com", "recipient@example.com");
    }

    @Test
    void shouldRejectDuplicateRecipientsAfterNormalization() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new EmailRecipients(List.of("recipient@Example.com", "recipient@example.com")));
    }

    @Test
    void shouldRejectInvalidRecipient() {
        assertThatIllegalArgumentException().isThrownBy(() -> new EmailRecipients(List.of("not-an-email")));
    }

}
