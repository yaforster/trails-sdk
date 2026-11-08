package io.github.yaforster.trails.core.test.action.download;

import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContexts;
import io.github.yaforster.trails.core.test.result.TechnicalFailure;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CheckDownloadedFileActionTest {

    @Test
    void execute_returnsSuccess_whenBrowserPortReportsExpectedFile() {
        Result result = action("report.pdf").execute(
                TestExecutionContexts.withBrowser(new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                        List.of(new FileData(new byte[]{1}, "report.pdf")))));

        assertInstanceOf(Success.class, result);
    }

    @Test
    void execute_reportsDownloadedFileNames_whenExpectedFileIsAbsent() {
        Result result = action("report.pdf").execute(
                TestExecutionContexts.withBrowser(new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                        List.of(new FileData(new byte[]{1}, "other.pdf")))));

        assertEquals("Downloaded file 'report.pdf' was not found. Downloaded files: other.pdf",
                result.getResultMessage());
    }

    @Test
    void execute_returnsValidationFailure_whenNoFilesWereDownloaded() {
        Result result = action("report.pdf").execute(
                TestExecutionContexts.withBrowser(new TestExecutionContexts.FakeBrowserSession()));

        assertInstanceOf(ValidationFailure.class, result);
    }

    @Test
    void execute_returnsTechnicalFailure_whenBrowserPortCannotReadDownloads() {
        Result result = action("report.pdf").execute(
                TestExecutionContexts.withBrowser(new TestExecutionContexts.FakeBrowserSession().downloadFailure(
                        new IllegalStateException("download failed"))));

        assertInstanceOf(TechnicalFailure.class, result);
    }

    private static CheckDownloadedFileAction action(String fileName) {
        return CheckDownloadedFileAction.builder()
                                        .actionID(1L)
                                        .label("download")
                                        .fileName(fileName)
                                        .build();
    }

}
