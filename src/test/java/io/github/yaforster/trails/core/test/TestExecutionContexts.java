package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportResizeOutcome;
import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import io.github.yaforster.trails.core.test.action.document.DocumentTextExtractionException;
import io.github.yaforster.trails.core.test.action.document.ExtractedDocumentContent;
import io.github.yaforster.trails.core.test.action.document.PdfVisualProof;
import io.github.yaforster.trails.core.test.action.element.ElementValueSource;
import io.github.yaforster.trails.core.test.action.element.Locator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class TestExecutionContexts {

    private TestExecutionContexts() {}

    public static TestExecutionContext withBrowser(FakeBrowserSession browser) {
        return withBrowser(browser, new FakeDocumentExecution());
    }

    public static TestExecutionContext withBrowser(FakeBrowserSession browser, DocumentExecution documents) {
        return new TestExecutionContext(new BrowserExecution(browser, documents));
    }

    public static final class FakeBrowserSession implements BrowserSession {

        private String screenshot = "screenshot";
        private RuntimeException screenshotFailure;
        private boolean elementExists;
        private RuntimeException elementExistsFailure;
        private String clickTagName = "button";
        private String typedTagName = "input";
        private String selectedTagName = "select";
        private BrowserElementText elementText = new BrowserElementText("div", "");
        private String elementValue;
        private Optional<String> cookieValue = Optional.empty();
        private String localStorageValue;
        private String sessionStorageValue;
        private RuntimeException operationFailure;
        private List<FileData> downloadedFiles = List.of();
        private RuntimeException downloadFailure;
        private final List<String> openedUrls = new ArrayList<>();
        private final List<ViewportMoveCall> viewportMoves = new ArrayList<>();
        private final List<ViewportCoordinates> coordinateClicks = new ArrayList<>();
        private final List<ViewportDimensions> viewportResizeRequests = new ArrayList<>();
        private ViewportResizeOutcome viewportResizeOutcome;
        private String renderedHtml;
        private int renderedScrollTop;
        private boolean downloadsCleared;
        private boolean closed;

        public FakeBrowserSession screenshot(String value) {
            this.screenshot = value;
            return this;
        }

        public FakeBrowserSession screenshotFailure(RuntimeException value) {
            this.screenshotFailure = value;
            return this;
        }

        public FakeBrowserSession elementExists(boolean value) {
            this.elementExists = value;
            return this;
        }

        public FakeBrowserSession elementExistsFailure(RuntimeException value) {
            this.elementExistsFailure = value;
            return this;
        }

        public FakeBrowserSession clickTagName(String value) {
            this.clickTagName = value;
            return this;
        }

        public FakeBrowserSession typedTagName(String value) {
            this.typedTagName = value;
            return this;
        }

        public FakeBrowserSession selectedTagName(String value) {
            this.selectedTagName = value;
            return this;
        }

        public FakeBrowserSession elementText(BrowserElementText value) {
            this.elementText = value;
            return this;
        }

        public FakeBrowserSession elementValue(String value) {
            this.elementValue = value;
            return this;
        }

        public FakeBrowserSession withCookieValue(String value) {
            this.cookieValue = Optional.ofNullable(value);
            return this;
        }

        public FakeBrowserSession withLocalStorageValue(String value) {
            this.localStorageValue = value;
            return this;
        }

        public FakeBrowserSession withSessionStorageValue(String value) {
            this.sessionStorageValue = value;
            return this;
        }

        public FakeBrowserSession operationFailure(RuntimeException value) {
            this.operationFailure = value;
            return this;
        }

        public FakeBrowserSession downloadedFiles(List<FileData> value) {
            this.downloadedFiles = value;
            return this;
        }

        public FakeBrowserSession downloadFailure(RuntimeException value) {
            this.downloadFailure = value;
            return this;
        }

        public FakeBrowserSession viewportResizeOutcome(ViewportResizeOutcome value) {
            this.viewportResizeOutcome = value;
            return this;
        }

        public List<String> openedUrls() {
            return List.copyOf(openedUrls);
        }

        public List<ViewportMoveCall> viewportMoves() {
            return List.copyOf(viewportMoves);
        }

        public List<ViewportCoordinates> coordinateClicks() {
            return List.copyOf(coordinateClicks);
        }

        public List<ViewportDimensions> viewportResizeRequests() {
            return List.copyOf(viewportResizeRequests);
        }

        public String renderedHtml() {
            return renderedHtml;
        }

        public int renderedScrollTop() {
            return renderedScrollTop;
        }

        public boolean downloadsCleared() {
            return downloadsCleared;
        }

        public boolean closed() {
            return closed;
        }

        @Override
        public void open(String url) {
            throwIfOperationFailed();
            openedUrls.add(url);
        }

        @Override
        public String captureScreenshot() {
            if (screenshotFailure != null) {
                throw screenshotFailure;
            }
            return screenshot;
        }

        @Override
        public String captureScreenshot(Locator locator) {
            return captureScreenshot();
        }

        @Override
        public boolean elementExists(Locator locator) {
            if (elementExistsFailure != null) {
                throw elementExistsFailure;
            }
            return elementExists;
        }

        @Override
        public String click(Locator locator) {
            throwIfOperationFailed();
            return clickTagName;
        }

        @Override
        public void click(ViewportCoordinates coordinates) {
            throwIfOperationFailed();
            coordinateClicks.add(coordinates);
        }

        @Override
        public ViewportResizeOutcome resizeViewport(ViewportDimensions dimensions) {
            throwIfOperationFailed();
            viewportResizeRequests.add(dimensions);
            return viewportResizeOutcome == null
                    ? new ViewportResizeOutcome.Success(dimensions, dimensions)
                    : viewportResizeOutcome;
        }

        @Override
        public String type(Locator locator, String value, boolean clearBeforeTyping, long delayAfterClearMillis) {
            throwIfOperationFailed();
            return typedTagName;
        }

        @Override
        public BrowserElementText readText(Locator locator) {
            throwIfOperationFailed();
            return elementText;
        }

        @Override
        public String readValue(Locator locator, ElementValueSource valueSource, String valueName) {
            throwIfOperationFailed();
            return elementValue;
        }

        @Override
        public String select(Locator locator, String label) {
            throwIfOperationFailed();
            return selectedTagName;
        }

        @Override
        public Optional<String> cookieValue(String name) {
            return cookieValue;
        }

        @Override
        public String localStorageValue(String key) {
            return localStorageValue;
        }

        @Override
        public String sessionStorageValue(String key) {
            return sessionStorageValue;
        }

        @Override
        public void moveViewport(ViewportMove movement, ViewportMoveDirection direction, double amount,
                ViewportMoveUnit unit) {
            throwIfOperationFailed();
            viewportMoves.add(new ViewportMoveCall(movement, direction, amount, unit));
        }

        @Override
        public void renderHtml(String html, int scrollTop) {
            renderedHtml = html;
            renderedScrollTop = scrollTop;
        }

        @Override
        public List<FileData> refreshDownloadedFiles() {
            if (downloadFailure != null) {
                throw downloadFailure;
            }
            return downloadedFiles;
        }

        @Override
        public void clearDownloadedFiles() {
            downloadsCleared = true;
        }

        @Override
        public void close() {
            closed = true;
        }

        private void throwIfOperationFailed() {
            if (operationFailure != null) {
                throw operationFailure;
            }
        }

    }

    public record ViewportMoveCall(ViewportMove movement, ViewportMoveDirection direction, double amount,
            ViewportMoveUnit unit) {
    }

    public static final class FakeDocumentExecution implements DocumentExecution {

        private boolean pdf;
        private ExtractedDocumentContent extracted = new ExtractedDocumentContent("document.txt", "", "Plain text");
        private Optional<PdfVisualProof> pdfProof = Optional.empty();
        private DocumentTextExtractionException failure;

        public FakeDocumentExecution pdf(boolean value) {
            this.pdf = value;
            return this;
        }

        public FakeDocumentExecution extracted(ExtractedDocumentContent value) {
            this.extracted = value;
            return this;
        }

        public FakeDocumentExecution pdfProof(PdfVisualProof value) {
            this.pdfProof = Optional.ofNullable(value);
            return this;
        }

        public FakeDocumentExecution failure(DocumentTextExtractionException value) {
            this.failure = value;
            return this;
        }

        @Override
        public boolean isPdf(FileData file) {
            return pdf;
        }

        @Override
        public ExtractedDocumentContent extract(FileData file) throws DocumentTextExtractionException {
            throwIfFailed();
            return extracted;
        }

        @Override
        public Optional<PdfVisualProof> renderPdfProof(FileData file, String expectedText, boolean caseSensitive)
                throws DocumentTextExtractionException {
            throwIfFailed();
            return pdfProof;
        }

        private void throwIfFailed() throws DocumentTextExtractionException {
            if (failure != null) {
                throw failure;
            }
        }

    }

}
