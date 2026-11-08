package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import io.github.yaforster.trails.core.test.action.element.ElementValueSource;
import io.github.yaforster.trails.core.test.action.element.Locator;

import java.util.List;
import java.util.Optional;

/**
 * Represents a Browser that Trails operates.
 */
public interface BrowserSession {

    void open(String url);

    String captureScreenshot();

    String captureScreenshot(Locator locator);

    boolean elementExists(Locator locator);

    String click(Locator locator);

    void click(ViewportCoordinates coordinates);

    ViewportResizeOutcome resizeViewport(ViewportDimensions dimensions);

    String type(Locator locator, String value, boolean clearBeforeTyping, long delayAfterClearMillis);

    BrowserElementText readText(Locator locator);

    String readValue(Locator locator, ElementValueSource valueSource, String valueName);

    String select(Locator locator, String label);

    Optional<String> cookieValue(String name);

    String localStorageValue(String key);

    String sessionStorageValue(String key);

    void moveViewport(ViewportMove movement, ViewportMoveDirection direction, double amount, ViewportMoveUnit unit);

    void renderHtml(String html, int scrollTop);

    List<FileData> refreshDownloadedFiles();

    void clearDownloadedFiles();

    void close();

}
