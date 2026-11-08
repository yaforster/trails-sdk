package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.ValidationViolation;
import lombok.Getter;

@Getter
public class TrailsScreenshotProcessingException extends RuntimeException {

    private final ValidationViolation validationViolation;

    public TrailsScreenshotProcessingException(ValidationViolation validationViolation) {
        super(validationViolation.message());
        this.validationViolation = validationViolation;
    }

    public static TrailsScreenshotProcessingException emptyFile() {
        return new TrailsScreenshotProcessingException(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_EMPTY",
                "Element screenshot file must not be empty.", "/file"));
    }

    public static TrailsScreenshotProcessingException tooLarge() {
        return new TrailsScreenshotProcessingException(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_TOO_LARGE",
                "Element screenshot file must not be larger than the configured maximum size.", "/file"));
    }

    public static TrailsScreenshotProcessingException unsupportedType() {
        return new TrailsScreenshotProcessingException(
                new ValidationViolation("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED",
                        "Element screenshot file must be a PNG, JPEG, GIF, BMP, or WebP image.", "/file"));
    }

    public static TrailsScreenshotProcessingException dimensionsTooLarge() {
        return new TrailsScreenshotProcessingException(
                new ValidationViolation("ELEMENT_SCREENSHOT_FILE_DIMENSIONS_TOO_LARGE",
                        "Element screenshot image width and height must not exceed the configured maximum dimensions.",
                        "/file"));
    }

    public static TrailsScreenshotProcessingException unreadableFile() {
        return new TrailsScreenshotProcessingException(new ValidationViolation("ELEMENT_SCREENSHOT_FILE_UNREADABLE",
                "Element screenshot file could not be read.", "/file"));
    }

    public static TrailsScreenshotProcessingException unreadableImage() {
        return new TrailsScreenshotProcessingException(
                new ValidationViolation("ELEMENT_SCREENSHOT_FILE_UNREADABLE_IMAGE",
                        "Element screenshot file must be a readable image.", "/file"));
    }

}
