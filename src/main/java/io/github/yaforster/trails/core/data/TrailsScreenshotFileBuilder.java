package io.github.yaforster.trails.core.data;

import java.awt.*;

public class TrailsScreenshotFileBuilder {

    private final long maxSizeBytes;
    private final int maxWidth;
    private final int maxHeight;

    public TrailsScreenshotFileBuilder(long maxSizeBytes, int maxWidth, int maxHeight) {
        this.maxSizeBytes = maxSizeBytes;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    public TrailsScreenshotFile build(String filename, byte[] content) {
        validateContent(content);

        return new TrailsScreenshotFile(filename, content);
    }

    private void validateContent(byte[] content) {
        if (content.length == 0) {
            throw TrailsScreenshotProcessingException.emptyFile();
        }
        if (content.length > maxSizeBytes) {
            throw TrailsScreenshotProcessingException.tooLarge();
        }

        ImageType imageType = ImageTypeDetector.detect(content);
        if (imageType == ImageType.UNSUPPORTED) {
            throw TrailsScreenshotProcessingException.unsupportedType();
        }

        Dimension dimension = ImageDimensionReader.read(content, imageType);
        if (dimension.width > maxWidth || dimension.height > maxHeight) {
            throw TrailsScreenshotProcessingException.dimensionsTooLarge();
        }
    }

}
