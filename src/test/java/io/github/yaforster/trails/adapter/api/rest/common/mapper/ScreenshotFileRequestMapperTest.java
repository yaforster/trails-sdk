package io.github.yaforster.trails.adapter.api.rest.common.mapper;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScreenshotFileRequestMapperTest {

    @Test
    void shouldExposeUnreadableUploadAsContractValidationFailure() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("disk failure"));
        ScreenshotFileRequestMapper mapper = new ScreenshotFileRequestMapper(
                new TrailsScreenshotFileBuilder(10, 10, 10));

        assertThatThrownBy(() -> mapper.from(file)).isInstanceOf(ApiRequestValidationException.class)
                                                   .extracting(
                                                           exception -> ((ApiRequestValidationException) exception).violations()
                                                                                                                   .getFirst()
                                                                                                                   .code())
                                                   .isEqualTo("ELEMENT_SCREENSHOT_FILE_UNREADABLE");
    }

    @Test
    void shouldExposeUnsupportedUploadAsContractValidationFailure() {
        MultipartFile file = mock(MultipartFile.class);
        try {
            when(file.getBytes()).thenReturn("not an image".getBytes());
        } catch (IOException exception) {
            throw new AssertionError(exception);
        }
        ScreenshotFileRequestMapper mapper = new ScreenshotFileRequestMapper(
                new TrailsScreenshotFileBuilder(1024, 100, 100));

        assertThatThrownBy(() -> mapper.from(file)).isInstanceOf(ApiRequestValidationException.class)
                                                   .extracting(
                                                           exception -> ((ApiRequestValidationException) exception).violations()
                                                                                                                   .getFirst()
                                                                                                                   .code())
                                                   .isEqualTo("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED");
    }

}
