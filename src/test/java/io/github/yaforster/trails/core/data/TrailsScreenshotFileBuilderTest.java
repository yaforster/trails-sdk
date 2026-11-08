package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TrailsScreenshotFileBuilderTest extends TrailsTest {

    @Test
    void build_shouldUseConfiguredMaxFileSize() {
        TrailsScreenshotFileBuilder builder = new TrailsScreenshotFileBuilder(1L, TEST_USER_IMAGE_MAX_WIDTH,
                TEST_USER_IMAGE_MAX_HEIGHT);

        TrailsScreenshotProcessingException exception = assertThrows(TrailsScreenshotProcessingException.class,
                () -> builder.build("image.png", pngBytes()));

        assertThat(exception.getValidationViolation()
                            .code()).isEqualTo("ELEMENT_SCREENSHOT_FILE_TOO_LARGE");
    }

    @Test
    void build_shouldUseConfiguredMaxWidth() {
        TrailsScreenshotFileBuilder builder = new TrailsScreenshotFileBuilder(TEST_USER_IMAGE_MAX_SIZE_BYTES, 0,
                TEST_USER_IMAGE_MAX_HEIGHT);

        TrailsScreenshotProcessingException exception = assertThrows(TrailsScreenshotProcessingException.class,
                () -> builder.build("image.png", pngBytes()));

        assertThat(exception.getValidationViolation()
                            .code()).isEqualTo("ELEMENT_SCREENSHOT_FILE_DIMENSIONS_TOO_LARGE");
    }

    @Test
    void build_shouldUseConfiguredMaxHeight() {
        TrailsScreenshotFileBuilder builder = new TrailsScreenshotFileBuilder(TEST_USER_IMAGE_MAX_SIZE_BYTES,
                TEST_USER_IMAGE_MAX_WIDTH, 0);

        TrailsScreenshotProcessingException exception = assertThrows(TrailsScreenshotProcessingException.class,
                () -> builder.build("image.png", pngBytes()));

        assertThat(exception.getValidationViolation()
                            .code()).isEqualTo("ELEMENT_SCREENSHOT_FILE_DIMENSIONS_TOO_LARGE");
    }

}
