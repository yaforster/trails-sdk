package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.TrailsTest;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageDimensionReaderTest extends TrailsTest {

    @Test
    void read_shouldReturnPngDimensions() {
        Dimension dimension = ImageDimensionReader.read(imageBytes("png", 4, 3), ImageType.PNG);

        assertThat(dimension).isEqualTo(new Dimension(4, 3));
    }

    @Test
    void read_shouldReturnVp8xWebpDimensions() {
        Dimension dimension = ImageDimensionReader.read(webpBytes(4, 3), ImageType.WEBP);

        assertThat(dimension).isEqualTo(new Dimension(4, 3));
    }

    @Test
    void read_shouldRejectUnreadableSupportedImage() {
        TrailsScreenshotProcessingException exception = assertThrows(TrailsScreenshotProcessingException.class,
                () -> ImageDimensionReader.read(pngSignatureBytes(), ImageType.PNG));

        assertThat(exception.getValidationViolation()
                            .code()).isEqualTo("ELEMENT_SCREENSHOT_FILE_UNREADABLE_IMAGE");
    }

    private byte[] webpBytes(int width, int height) {
        byte[] bytes = new byte[30];
        System.arraycopy("RIFF".getBytes(), 0, bytes, 0, 4);
        bytes[4] = 22;
        System.arraycopy("WEBP".getBytes(), 0, bytes, 8, 4);
        System.arraycopy("VP8X".getBytes(), 0, bytes, 12, 4);
        bytes[16] = 10;
        int storedWidth = width - 1;
        int storedHeight = height - 1;
        bytes[24] = (byte) storedWidth;
        bytes[25] = (byte) (storedWidth >> 8);
        bytes[26] = (byte) (storedWidth >> 16);
        bytes[27] = (byte) storedHeight;
        bytes[28] = (byte) (storedHeight >> 8);
        bytes[29] = (byte) (storedHeight >> 16);
        return bytes;
    }

}
