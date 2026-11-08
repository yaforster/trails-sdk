package io.github.yaforster.trails.sdk.composition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class TrailsBrowserRegistrationTest {

    @Test
    void shouldRejectAnIncompleteBrowserRegistration() {
        assertThatNullPointerException().isThrownBy(
                () -> new TrailsBrowserRegistration(new AdapterId("test-browser"), null));
    }

}
