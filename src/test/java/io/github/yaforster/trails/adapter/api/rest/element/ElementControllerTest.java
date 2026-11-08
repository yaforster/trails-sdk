package io.github.yaforster.trails.adapter.api.rest.element;

import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import io.github.yaforster.trails.core.TrailsTest;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.TrailsScreenshotFileBuilder;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ElementControllerTest extends TrailsTest {

    @Test
    void shouldPersistValidatedScreenshotAndReturnItsArtifact() {
        ElementDatabaseService elements = mock(ElementDatabaseService.class);
        ScreenshotDatabaseService screenshots = mock(ScreenshotDatabaseService.class);
        PersistedElement element = new PersistedElement(34L, "Login", LocatorType.CSS, 12L, 23L, "#login",
                ElementType.BUTTON);
        when(elements.getElement(new ElementDatabaseService.ElementDetails(12L, 23L, 34L, false))).thenReturn(
                Optional.of(element));
        when(screenshots.findIdByElementId(34L)).thenReturn(Optional.of(56L));
        ElementController controller = controller(elements, screenshots);
        MockMultipartFile upload = new MockMultipartFile("file", "login.png", "image/png", pngBytes());

        ResponseEntity<ArtifactDTO> response = controller.uploadElementScreenshot(12L, 23L, 34L, upload);

        assertThat(response).satisfies(result -> {
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody()
                             .getId()).isEqualTo(56L);
            assertThat(result.getBody()
                             .getType()
                             .name()).isEqualTo("SCREENSHOT");
            assertThat(result.getBody()
                             .getFilename()).isEqualTo("login.png");
            assertThat(result.getBody()
                             .getLinks()
                             .get("self")
                             .getHref()).isEqualTo("/api/applications/12/stages/23/elements/34/screenshot");
        });
        verify(screenshots).setScreenshot(org.mockito.ArgumentMatchers.argThat(saved -> saved.elementId()
                                                                                             .equals(34L)
                && saved.file()
                        .filename()
                        .equals("login.png")
                && saved.file()
                        .content().length == pngBytes().length));
    }

    private ElementController controller(ElementDatabaseService elements, ScreenshotDatabaseService screenshots) {
        TrailsAuthorization authorization = mock(TrailsAuthorization.class);
        return new ElementController(elements, screenshots, mock(StageDatabaseService.class), authorization,
                new ElementRequestValidator(elements),
                new ScreenshotFileRequestMapper(new TrailsScreenshotFileBuilder(TEST_USER_IMAGE_MAX_SIZE_BYTES,
                        TEST_USER_IMAGE_MAX_WIDTH, TEST_USER_IMAGE_MAX_HEIGHT)),
                new ElementResponseAssembler());
    }

}
