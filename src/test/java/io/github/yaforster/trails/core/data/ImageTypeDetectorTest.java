package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTypeDetectorTest extends TrailsTest {

    @Test
    void detect_shouldIdentifySupportedImageTypes() {
        assertThat(ImageTypeDetector.detect(pngBytes())).isEqualTo(ImageType.PNG);
    }

    @Test
    void detect_shouldIdentifyJpeg() {
        assertThat(ImageTypeDetector.detect(jpegBytes())).isEqualTo(ImageType.JPEG);
    }

    @Test
    void detect_shouldIdentifyGif() {
        assertThat(ImageTypeDetector.detect(imageBytes("gif", 1, 1))).isEqualTo(ImageType.GIF);
    }

    @Test
    void detect_shouldIdentifyBmp() {
        assertThat(ImageTypeDetector.detect(imageBytes("bmp", 1, 1))).isEqualTo(ImageType.BMP);
    }

    @Test
    void detect_shouldIdentifyWebpFromRiffAndWebpFragments() {
        byte[] content = "RIFFxxxxWEBP".getBytes(StandardCharsets.US_ASCII);

        assertThat(ImageTypeDetector.detect(content)).isEqualTo(ImageType.WEBP);
    }

    @Test
    void detect_shouldRejectIncompleteSignature() {
        assertThat(ImageTypeDetector.detect(Arrays.copyOf(pngSignatureBytes(), 7))).isEqualTo(ImageType.UNSUPPORTED);
    }

    @Test
    void detect_shouldRejectUnsupportedContent() {
        assertThat(ImageTypeDetector.detect("not an image".getBytes(StandardCharsets.US_ASCII))).isEqualTo(
                ImageType.UNSUPPORTED);
    }

    @Test
    void supportedImageTypes_shouldReturnPublishedFormatOrder() {
        assertThat(ImageTypeDetector.supportedImageTypes()).containsExactly(ImageType.PNG, ImageType.JPEG,
                ImageType.GIF, ImageType.BMP, ImageType.WEBP);
    }

}
