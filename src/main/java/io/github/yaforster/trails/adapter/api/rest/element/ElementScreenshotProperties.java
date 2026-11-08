package io.github.yaforster.trails.adapter.api.rest.element;

import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * Configures the server-side limits shared by element screenshots and profile pictures.
 *
 * @param maxSize
 *            maximum accepted file size
 * @param maxWidth
 *            maximum accepted image width
 * @param maxHeight
 *            maximum accepted image height
 */
@ConfigurationProperties("trails.element.screenshot")
public record ElementScreenshotProperties(DataSize maxSize, Integer maxWidth, Integer maxHeight) {

    private static final DataSize DEFAULT_MAX_SIZE = DataSize.ofBytes(5L * 1024L * 1024L);
    private static final int DEFAULT_MAX_DIMENSION = 1920;

    public ElementScreenshotProperties {
        maxSize = Optional.ofNullable(maxSize)
                          .orElse(DEFAULT_MAX_SIZE);
        maxWidth = Optional.ofNullable(maxWidth)
                           .orElse(DEFAULT_MAX_DIMENSION);
        maxHeight = Optional.ofNullable(maxHeight)
                            .orElse(DEFAULT_MAX_DIMENSION);
        if (maxSize.toBytes() <= 0) {
            throw new IllegalArgumentException("trails.element.screenshot.max-size must be positive");
        }
        if (maxWidth <= 0) {
            throw new IllegalArgumentException("trails.element.screenshot.max-width must be positive");
        }
        if (maxHeight <= 0) {
            throw new IllegalArgumentException("trails.element.screenshot.max-height must be positive");
        }
    }

}
