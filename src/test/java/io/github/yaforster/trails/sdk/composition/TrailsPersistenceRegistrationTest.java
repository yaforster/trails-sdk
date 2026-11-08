package io.github.yaforster.trails.sdk.composition;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.Mockito.mock;

class TrailsPersistenceRegistrationTest {

    @Test
    void shouldRejectAnIncompletePersistencePortSet() {
        assertThatNullPointerException().isThrownBy(
                () -> new TrailsPersistenceRegistration.Ports(mock(), mock(), mock(), mock(), mock(), mock(), mock(),
                        mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), null));
    }

}
