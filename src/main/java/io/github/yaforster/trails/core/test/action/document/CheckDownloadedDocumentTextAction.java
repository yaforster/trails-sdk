package io.github.yaforster.trails.core.test.action.document;

import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.FileData;
import io.github.yaforster.trails.core.test.Result;
import io.github.yaforster.trails.core.test.TestExecutionContext;
import io.github.yaforster.trails.core.test.result.ValidationFailure;
import io.github.yaforster.trails.core.test.result.Success;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@SuperBuilder
@Getter
public class CheckDownloadedDocumentTextAction extends Action {

    private static final String EXPECTED_TEXT_FOUND_MESSAGE = "Downloaded document '%s' contains the expected text.";
    private static final String EXPECTED_TEXT_FOUND_ON_PAGE_MESSAGE = "Downloaded document '%s' contains the expected text on page %s.";
    private static final String EXPECTED_TEXT_NOT_FOUND_MESSAGE = "Downloaded document '%s' does not contain the expected text.";
    private static final String DOCUMENT_NOT_FOUND_MESSAGE = "Downloaded document '%s' was not found. %s";
    private static final String DOCUMENT_READ_FAILURE_MESSAGE = "Downloaded document '%s' could not be opened or read: %s";
    private static final String TECHNICAL_FAILURE_MESSAGE = "Could not verify downloaded document text.";
    private static final String NO_DOWNLOADED_FILES_MESSAGE = "No files were downloaded.";
    private static final String DOWNLOADED_FILES_MESSAGE = "Downloaded files: %s";
    private final String fileName;
    private final String expectedText;
    private final boolean caseSensitive;

    @Override
    public Result execute(TestExecutionContext context) {
        try {
            return checkDownloadedDocument(context);
        } catch (DocumentTextExtractionException extractionFailure) {
            try {
                renderFailureProofInBrowser(context, extractionFailure.getMessage());
            } catch (RuntimeException evidenceFailure) {
                extractionFailure.addSuppressed(evidenceFailure);
            }
            return validationFailure(context,
                    DOCUMENT_READ_FAILURE_MESSAGE.formatted(fileName, extractionFailure.getMessage()));
        } catch (Exception exception) {
            return generateTechnicalFailureResult(exception, TECHNICAL_FAILURE_MESSAGE, context);
        }
    }

    private Result checkDownloadedDocument(TestExecutionContext context) throws DocumentTextExtractionException {
        List<FileData> downloadedFiles = context.refreshDownloadedFiles();
        Optional<FileData> downloadedFile = findDownloadedFile(downloadedFiles);
        if (downloadedFile.isEmpty()) {
            return reportMissingFile(context, downloadedFiles);
        }
        FileData file = downloadedFile.get();
        if (context.documents()
                   .isPdf(file)) {
            return checkPdfText(context, file);
        }
        return checkExtractedText(context, file);
    }

    private ValidationFailure reportMissingFile(TestExecutionContext context, List<FileData> downloadedFiles) {
        String message = DOCUMENT_NOT_FOUND_MESSAGE.formatted(fileName, downloadedFilesMessage(downloadedFiles));
        DocumentTextProof proof = DocumentTextProof.missingFile(fileName, expectedText, message);
        renderProofInBrowser(context, proof);
        return validationFailure(context, message);
    }

    private Result checkExtractedText(TestExecutionContext context, FileData file)
            throws DocumentTextExtractionException {
        ExtractedDocumentContent extractedContent = context.documents()
                                                           .extract(file);
        boolean found = containsExpectedText(extractedContent.text());
        DocumentTextProof proof = DocumentTextProof.from(extractedContent, expectedText, caseSensitive, found);
        renderProofInBrowser(context, proof);
        if (found) {
            return success(context, EXPECTED_TEXT_FOUND_MESSAGE.formatted(fileName));
        }
        return validationFailure(context, EXPECTED_TEXT_NOT_FOUND_MESSAGE.formatted(fileName));
    }

    private Result checkPdfText(TestExecutionContext context, FileData file) throws DocumentTextExtractionException {
        Optional<PdfVisualProof> visualProofResult = context.documents()
                                                            .renderPdfProof(file, expectedText, caseSensitive);
        if (visualProofResult.isEmpty()) {
            return reportValidationFailure(context, file);
        }
        return reportValidationSuccess(context, visualProofResult.get());
    }

    private Success reportValidationSuccess(TestExecutionContext context, PdfVisualProof proof) {
        String successMessage = EXPECTED_TEXT_FOUND_ON_PAGE_MESSAGE.formatted(fileName, proof.pageNumber());

        renderProofInBrowser(context, proof);
        return success(context, successMessage);
    }

    private ValidationFailure reportValidationFailure(TestExecutionContext context, FileData file)
            throws DocumentTextExtractionException {
        ExtractedDocumentContent extractedDocumentContent = context.documents()
                                                                   .extract(file);
        DocumentTextProof proof = DocumentTextProof.from(extractedDocumentContent, expectedText, caseSensitive, false);
        String failureMessage = EXPECTED_TEXT_NOT_FOUND_MESSAGE.formatted(fileName);

        renderProofInBrowser(context, proof);
        return validationFailure(context, failureMessage);
    }

    private Optional<FileData> findDownloadedFile(List<FileData> downloadedFiles) {
        return downloadedFiles.stream()
                              .filter(file -> StringUtils.equals(fileName, file.fileName()))
                              .findFirst();
    }

    private boolean containsExpectedText(String documentText) {
        if (StringUtils.isBlank(expectedText)) {
            return false;
        }
        if (caseSensitive) {
            return Strings.CS.contains(documentText, expectedText);
        }
        return Strings.CI.contains(documentText, expectedText);
    }

    private ValidationFailure validationFailure(TestExecutionContext context, String message) {
        return ValidationFailure.builder()
                                .actionID(getActionID())
                                .label(getLabel())
                                .resultMessage(message)
                                .base64Screenshot(captureScreenshotIfAvailable(context))
                                .build();
    }

    private Success success(TestExecutionContext context, String message) {
        return Success.builder()
                      .actionID(getActionID())
                      .label(getLabel())
                      .resultMessage(message)
                      .base64Screenshot(getPageScreenshot(context))
                      .build();
    }

    private void renderFailureProofInBrowser(TestExecutionContext context, String message) {
        DocumentTextProof proof = DocumentTextProof.openFailure(fileName, expectedText, message);
        renderProofInBrowser(context, proof);
    }

    private void renderProofInBrowser(TestExecutionContext context, DocumentTextProof proof) {
        context.browser()
               .renderHtml(proof.toHtml(), 0);
    }

    private void renderProofInBrowser(TestExecutionContext context, PdfVisualProof proof) {
        context.browser()
               .renderHtml(proof.toHtml(), proof.scrollTop());
    }

    private String downloadedFilesMessage(List<FileData> downloadedFiles) {
        if (downloadedFiles.isEmpty()) {
            return NO_DOWNLOADED_FILES_MESSAGE;
        }
        String downloadedFileNames = downloadedFiles.stream()
                                                    .map(FileData::fileName)
                                                    .collect(Collectors.joining(", "));
        return DOWNLOADED_FILES_MESSAGE.formatted(downloadedFileNames);
    }

}
