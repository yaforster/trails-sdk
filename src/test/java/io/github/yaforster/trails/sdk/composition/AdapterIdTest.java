package io.github.yaforster.trails.sdk.composition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class AdapterIdTest {

    @Test
    void shouldRejectIdentifierOutsideThePublishedFormat() {
        assertThatIllegalArgumentException().isThrownBy(() -> new AdapterId("SMTP notifier"));
    }

}
