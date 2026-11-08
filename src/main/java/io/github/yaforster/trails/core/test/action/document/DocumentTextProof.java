package io.github.yaforster.trails.core.test.action.document;

import org.apache.commons.lang3.StringUtils;

record DocumentTextProof(String title, String fileName, String expectedText, String status, String body) {

    private static final int CONTEXT_LENGTH = 900;

    static DocumentTextProof from(ExtractedDocumentContent extractedDocumentContent, String expectedText,
            boolean caseSensitive, boolean found) {
        String extractedText = extractedDocumentContent.text();
        String sourceType = extractedDocumentContent.sourceType();
        String body;
        String status;
        if (found) {
            body = highlightedContext(extractedText, expectedText, caseSensitive);
            status = "Text found in extracted " + sourceType + " content.";
        } else {
            body = escape(excerpt(extractedText));
            status = "Text was not found in extracted " + sourceType + " content.";
        }
        return new DocumentTextProof("Downloaded Document Text Check", extractedDocumentContent.fileName(),
                expectedText, status, body);
    }

    static DocumentTextProof missingFile(String fileName, String expectedText, String message) {
        return new DocumentTextProof("Downloaded Document Text Check", fileName, expectedText, message, "");
    }

    static DocumentTextProof openFailure(String fileName, String expectedText, String message) {
        return new DocumentTextProof("Downloaded Document Text Check", fileName, expectedText, message, "");
    }

    private static String highlightedContext(String text, String expectedText, boolean caseSensitive) {
        int index = indexOf(text, expectedText, caseSensitive);
        if (index < 0) {
            return escape(excerpt(text));
        }

        int start = Math.max(0, index - CONTEXT_LENGTH / 2);
        int end = Math.min(text.length(), index + expectedText.length() + CONTEXT_LENGTH / 2);

        String prefix = "";
        if (start > 0) {
            prefix = "... ";
        }

        String suffix = "";
        if (end < text.length()) {
            suffix = " ...";
        }

        String textBeforeMatch = escape(prefix + text.substring(start, index));
        String matchingText = escape(text.substring(index, index + expectedText.length()));
        String textAfterMatch = escape(text.substring(index + expectedText.length(), end) + suffix);

        return textBeforeMatch + "<mark>" + matchingText + "</mark>" + textAfterMatch;
    }

    private static String excerpt(String text) {
        if (StringUtils.isBlank(text)) {
            return "No text could be extracted from the document.";
        }
        if (text.length() <= CONTEXT_LENGTH) {
            return text;
        }
        return text.substring(0, CONTEXT_LENGTH) + " ...";
    }

    private static int indexOf(String text, String expectedText, boolean caseSensitive) {
        if (text == null || StringUtils.isBlank(expectedText)) {
            return -1;
        }
        if (caseSensitive) {
            return StringUtils.indexOf(text, expectedText);
        }
        return StringUtils.indexOfIgnoreCase(text, expectedText);
    }

    private static String escape(String value) {
        return StringUtils.defaultString(value)
                          .replace("&", "&amp;")
                          .replace("<", "&lt;")
                          .replace(">", "&gt;")
                          .replace("\"", "&quot;");
    }

    String toHtml() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>%s</title>
                  <style>
                    body { color: #18181b; font-family: Arial, sans-serif; margin: 32px; }
                    main { display: grid; gap: 18px; max-width: 1100px; }
                    h1 { font-size: 28px; margin: 0; }
                    dl { display: grid; grid-template-columns: max-content minmax(0, 1fr); gap: 8px 18px; margin: 0; }
                    dt { color: #52525b; font-weight: 700; }
                    dd { margin: 0; overflow-wrap: anywhere; }
                    pre { background: #f8fafc; border: 1px solid #cbd5e1; border-radius: 8px; font: 16px/1.55 Consolas, monospace; margin: 0; padding: 18px; white-space: pre-wrap; }
                    mark { background: #fde047; color: #111827; font-weight: 700; padding: 0 2px; }
                  </style>
                </head>
                <body>
                  <main>
                    <h1>%s</h1>
                    <dl>
                      <dt>File</dt><dd>%s</dd>
                      <dt>Expected text</dt><dd>%s</dd>
                      <dt>Status</dt><dd>%s</dd>
                    </dl>
                    <pre>%s</pre>
                  </main>
                </body>
                </html>
                """.formatted(
                escape(title), escape(title), escape(fileName), escape(expectedText), escape(status), body);
    }

}
