package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import io.github.yaforster.trails.core.data.TrailsScreenshotProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;

/** Converts a multipart image into a content-validated Trails image file. */
public final class ScreenshotFileRequestMapper {

    private final TrailsScreenshotFileBuilder files;

    public ScreenshotFileRequestMapper(TrailsScreenshotFileBuilder files) {
        this.files = Objects.requireNonNull(files, "Screenshot file builder is required");
    }

    public TrailsScreenshotFile from(MultipartFile upload) {
        try {
            return files.build(upload.getOriginalFilename(), upload.getBytes());
        } catch (IOException exception) {
            throw invalid(TrailsScreenshotProcessingException.unreadableFile());
        } catch (TrailsScreenshotProcessingException exception) {
            throw invalid(exception);
        }
    }

    private ApiRequestValidationException invalid(TrailsScreenshotProcessingException exception) {
        return new ApiRequestValidationException(List.of(exception.getValidationViolation()));
    }

}
