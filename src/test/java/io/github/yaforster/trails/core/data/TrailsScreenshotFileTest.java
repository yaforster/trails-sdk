package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TrailsScreenshotFileTest extends TrailsTest {

    @Test
    void imageType_shouldDetectPng() {
        TrailsScreenshotFile file = screenshotFile("image.png", pngBytes());

        assertThat(file.imageType()).isEqualTo(ImageType.PNG);
    }

    @Test
    void filename_shouldKeepClientFilenameAsMetadata() {
        TrailsScreenshotFile file = screenshotFile("renamed.txt", pngBytes());

        assertThat(file.filename()).isEqualTo("renamed.txt");
    }

    @Test
    void size_shouldReturnContentLength() {
        byte[] content = pngBytes();
        TrailsScreenshotFile file = screenshotFile("image.png", content);

        assertThat(file.size()).isEqualTo(content.length);
    }

    @Test
    void content_shouldDefensivelyCopyInput() {
        byte[] content = pngBytes();
        TrailsScreenshotFile file = screenshotFile("image.png", content);
        content[1] = 0x00;

        assertThat(file.content()[1]).isEqualTo((byte) 0x50);
    }

    @Test
    void content_shouldNotExposeInternalArray() {
        TrailsScreenshotFile file = screenshotFile("image.png", pngBytes());
        byte[] content = file.content();
        content[1] = 0x00;

        assertThat(file.content()[1]).isEqualTo((byte) 0x50);
    }

}
