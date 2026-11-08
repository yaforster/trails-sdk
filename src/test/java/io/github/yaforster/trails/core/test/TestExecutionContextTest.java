package io.github.yaforster.trails.core.test;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestExecutionContextTest {

    @Test
    void downloadedFiles_cachesTheFirstBrowserPortResult() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(new FileData(new byte[]{1}, "first.txt")));
        TestExecutionContext context = TestExecutionContexts.withBrowser(browser);
        context.downloadedFiles();
        browser.downloadedFiles(List.of(new FileData(new byte[]{2}, "second.txt")));

        assertEquals("first.txt", context.downloadedFiles()
                                         .getFirst()
                                         .fileName());
    }

    @Test
    void refreshDownloadedFiles_replacesTheCachedBrowserPortResult() {
        TestExecutionContexts.FakeBrowserSession browser = new TestExecutionContexts.FakeBrowserSession().downloadedFiles(
                List.of(new FileData(new byte[]{1}, "first.txt")));
        TestExecutionContext context = TestExecutionContexts.withBrowser(browser);
        context.downloadedFiles();
        browser.downloadedFiles(List.of(new FileData(new byte[]{2}, "second.txt")));

        assertEquals("second.txt", context.refreshDownloadedFiles()
                                          .getFirst()
                                          .fileName());
    }

    @Test
    void downloadedFiles_returnsEmptyList_whenBrowserPortReturnsNull() {
        TestExecutionContext context = TestExecutionContexts.withBrowser(
                new TestExecutionContexts.FakeBrowserSession().downloadedFiles(null));

        assertEquals(List.of(), context.downloadedFiles());
    }

}
