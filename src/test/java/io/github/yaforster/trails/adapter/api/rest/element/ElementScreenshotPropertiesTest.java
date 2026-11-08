package io.github.yaforster.trails.adapter.api.rest.element;

import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class ElementScreenshotPropertiesTest {

    @Test
    void shouldApplyDefaultsWhenScreenshotLimitsAreOmitted() {
        ElementScreenshotProperties properties = new ElementScreenshotProperties(null, null, null);

        assertThat(properties).isEqualTo(new ElementScreenshotProperties(DataSize.ofMegabytes(5), 1920, 1920));
    }

    @Test
    void shouldRejectNonPositiveScreenshotSize() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new ElementScreenshotProperties(DataSize.ofBytes(0), 1920, 1920))
                                            .withMessage("trails.element.screenshot.max-size must be positive");
    }

    @Test
    void shouldRejectNonPositiveScreenshotWidth() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> new ElementScreenshotProperties(DataSize.ofBytes(1), 0, 1920))
                                            .withMessage("trails.element.screenshot.max-width must be positive");
    }

}
