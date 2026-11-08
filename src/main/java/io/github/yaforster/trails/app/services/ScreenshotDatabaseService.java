package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.data.Screenshot;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ScreenshotDatabaseService {

    Map<Long, Long> findAllIdsByElementIds(ElementScreenshotIds elementScreenshotIds);

    Optional<Long> findIdByElementId(Long elementId);

    Optional<Screenshot> loadScreenshot(Long screenshotID);

    Optional<Screenshot> loadScreenshotByElementId(Long elementId);

    DatabaseDeletionResult deleteElementScreenshot(Long elementID);

    void setScreenshot(ElementScreenshot elementScreenshot);

    record ElementScreenshot(Long elementId, TrailsScreenshotFile file) {
    }

    record ElementScreenshotIds(List<Long> elementIds) {

        public ElementScreenshotIds {
            elementIds = List.copyOf(elementIds);
        }

    }

}
