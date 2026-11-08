package io.github.yaforster.trails.core.test.action.document;

import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CheckDownloadedDocumentTextActionTest {

    private static CheckDownloadedDocumentTextAction action(String fileName, String expectedText) {
        return CheckDownloadedDocumentTextAction.builder()
                                                .actionID(1L)
                                                .label("document")
                                                .fileName(fileName)
                                                .expectedText(expectedText)
                                                .caseSensitive(false)
                                                .build();
    }

    private static FileData file(String name) {
        return new FileData("content".getBytes(StandardCharsets.UTF_8), name);
    }

    @Test
    void execute_returnsSuccessAndRendersPlainTextProof_whenExpectedTextExists() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("report.txt")));
        TestExecutionContexts.FakeDocumentExecution documents = new TestExecutionContexts.FakeDocumentExecution().extracted(
                new ExtractedDocumentContent("report.txt", "Quarterly Report Approved", "Plain text"));

        Result result = action("report.txt", "report approved").execute(
                TestExecutionContexts.withBrowser(browser, documents));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void execute_escapesAndHighlightsTheMatchedPlainTextProof() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("report.txt")));
        TestExecutionContexts.FakeDocumentExecution documents = new TestExecutionContexts.FakeDocumentExecution().extracted(
                new ExtractedDocumentContent("report.txt", "<Report Approved>", "Plain text"));

        action("report.txt", "Report Approved").execute(TestExecutionContexts.withBrowser(browser, documents));

        assertTrue(browser.renderedHtml()
                          .contains("&lt;<mark>Report Approved</mark>&gt;"));
    }

    @Test
    void execute_returnsValidationFailureAndProof_whenDownloadedFileIsMissing() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("other.txt")));

        Result result = action("report.txt", "expected").execute(TestExecutionContexts.withBrowser(browser));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void execute_returnsValidationFailure_whenPdfProofDoesNotContainExpectedText() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("report.pdf")));
        TestExecutionContexts.FakeDocumentExecution documents = new TestExecutionContexts.FakeDocumentExecution().pdf(
                true)
                                                                                                                 .extracted(
                                                                                                                         new ExtractedDocumentContent(
                                                                                                                                 "report.pdf",
                                                                                                                                 "Actual content",
                                                                                                                                 "PDF"));

        Result result = action("report.pdf", "expected").execute(TestExecutionContexts.withBrowser(browser, documents));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void execute_rendersPdfProofAtProvidedScrollPosition() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("report.pdf")));
        TestExecutionContexts.FakeDocumentExecution documents = new TestExecutionContexts.FakeDocumentExecution().pdf(
                true)
                                                                                                                 .pdfProof(
                                                                                                                         new PdfVisualProof(
                                                                                                                                 "report.pdf",
                                                                                                                                 "expected",
                                                                                                                                 2,
                                                                                                                                 240,
                                                                                                                                 800,
                                                                                                                                 1200,
                                                                                                                                 "png"));

        action("report.pdf", "expected").execute(TestExecutionContexts.withBrowser(browser, documents));

        assertEquals(240, browser.renderedScrollTop());
    }

    @Test
    void execute_returnsValidationFailure_whenDocumentPortCannotReadFile() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("report.txt")));
        TestExecutionContexts.FakeDocumentExecution documents = new TestExecutionContexts.FakeDocumentExecution().failure(
                new DocumentTextExtractionException("corrupt"));

        Result result = action("report.txt", "expected").execute(TestExecutionContexts.withBrowser(browser, documents));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void shouldPreserveDocumentValidationFailure_whenProofAndScreenshotRenderingFail() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(file("report.txt")))
                                                                                                         .operationFailure(
                                                                                                                 new IllegalStateException(
                                                                                                                         "renderer unavailable"))
                                                                                                         .screenshotFailure(
                                                                                                                 new IllegalStateException(
                                                                                                                         "screenshot unavailable"));
        TestExecutionContexts.FakeDocumentExecution documents = new TestExecutionContexts.FakeDocumentExecution().failure(
                new DocumentTextExtractionException("corrupt"));

        Result result = action("report.txt", "expected").execute(TestExecutionContexts.withBrowser(browser, documents));

        assertInstanceOf(ValidationFailure.class, result);
    }

}
