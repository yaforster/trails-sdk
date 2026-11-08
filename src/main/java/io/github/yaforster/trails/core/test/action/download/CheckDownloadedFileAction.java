package io.github.yaforster.trails.core.test.action.download;

import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuperBuilder
@Getter
public class CheckDownloadedFileAction extends Action {

    private final String fileName;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            List<FileData> downloadedFiles = context.refreshDownloadedFiles();
            if (containsExpectedFile(downloadedFiles)) {
                return downloadedFileFound(context);
            }
            return downloadedFileMissing(context, downloadedFiles);
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, "Could not verify downloaded files.", context);
        }
    }

    private boolean containsExpectedFile(List<FileData> downloadedFiles) {
        return downloadedFiles.stream()
                              .map(FileData::fileName)
                              .anyMatch(downloadedFileName -> Objects.equals(fileName, downloadedFileName));
    }

    private Success downloadedFileFound(TestExecutionContext context) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage("Downloaded file '" + fileName + "' exists.")
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private ValidationFailure downloadedFileMissing(TestExecutionContext context, List<FileData> downloadedFiles) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .label(getLabel())
                                .resultMessage("Downloaded file '" + fileName + "' was not found. "
                                        + downloadedFilesMessage(downloadedFiles))
                                .base64Screenshot(getPageScreenshot(context))
                                .build();
    }

    private String downloadedFilesMessage(List<FileData> downloadedFiles) {
        if (downloadedFiles.isEmpty()) {
            return "No files were downloaded.";
        }
        return "Downloaded files: " + downloadedFiles.stream()
                                                     .map(FileData::fileName)
                                                     .collect(Collectors.joining(", "));
    }

}
