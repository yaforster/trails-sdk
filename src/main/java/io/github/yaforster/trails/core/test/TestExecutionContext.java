package io.github.yaforster.trails.core.test;

import io.github.yaforster.trails.core.test.action.document.DocumentExecution;
import io.github.yaforster.trails.core.test.action.value.ValueComputationInstruction;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

/**
 * Run-scoped context passed to actions while a single test path is executed.
 * <p>
 * The context exposes framework-free browser, download, and document execution ports. Actions may refresh downloads;
 * the latest inventory is retained for path persistence.
 */
public class TestExecutionContext {

    private final BrowserExecution execution;
    private final Clock clock;
    private List<FileData> downloadedFiles;

    public TestExecutionContext(BrowserExecution execution) {
        this(execution, Clock.systemDefaultZone());
    }

    public TestExecutionContext(BrowserExecution execution, Clock clock) {
        this.execution = execution;
        this.clock = Objects.requireNonNull(clock, "Execution clock is required");
    }

    public String compute(ValueComputationInstruction instruction) {
        return instruction.computeValue(clock);
    }

    public BrowserSession browser() {
        return execution.browser();
    }

    public DocumentExecution documents() {
        return execution.documents();
    }

    public List<FileData> downloadedFiles() {
        if (downloadedFiles == null) {
            return refreshDownloadedFiles();
        }
        return downloadedFiles;
    }

    public List<FileData> refreshDownloadedFiles() {
        downloadedFiles = downloadFiles();
        return downloadedFiles;
    }

    private List<FileData> downloadFiles() {
        List<FileData> files = browser().refreshDownloadedFiles();
        if (files == null) {
            return List.of();
        }
        return files;
    }

}
