package io.github.yaforster.trails.core.data;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

public final class ImageDimensionReader {

    private ImageDimensionReader() {}

    public static Dimension read(byte[] content, ImageType imageType) {
        if (imageType == ImageType.WEBP) {
            return WebpImageDimensionReader.read(content);
        }
        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            if (!imageReaders.hasNext()) {
                throw TrailsScreenshotProcessingException.unreadableImage();
            }
            ImageReader imageReader = imageReaders.next();
            try {
                imageReader.setInput(imageInputStream, true, true);
                return new Dimension(imageReader.getWidth(0), imageReader.getHeight(0));
            } finally {
                imageReader.dispose();
            }
        } catch (IOException exception) {
            throw TrailsScreenshotProcessingException.unreadableImage();
        }
    }

}
