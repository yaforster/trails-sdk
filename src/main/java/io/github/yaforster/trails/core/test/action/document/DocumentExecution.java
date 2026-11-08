package io.github.yaforster.trails.core.test.action.document;

import io.github.yaforster.trails.core.test.FileData;

import java.util.Optional;

public interface DocumentExecution {

    /**
     * Returns whether the filename extension or file signature suggests that this file contains a PDF.
     */
    boolean isPdf(FileData file);

    ExtractedDocumentContent extract(FileData file) throws DocumentTextExtractionException;

    Optional<PdfVisualProof> renderPdfProof(FileData file, String expectedText, boolean caseSensitive)
            throws DocumentTextExtractionException;

}
