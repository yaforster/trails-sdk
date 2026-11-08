package io.github.yaforster.trails.core.test.action.document;

import org.apache.commons.lang3.StringUtils;

public record PdfVisualProof(String fileName, String expectedText, int pageNumber, int scrollTop, int pageImageWidth,
        int pageImageHeight, String pageImageBase64) {

    public String toHtml() {
        return """
                <!doctype html>
                <html lang="en">
                <head>
                  <meta charset="utf-8">
                  <title>Downloaded Document Text Check</title>
                  <style>
                    body { background: #52525b; color: #18181b; font-family: Arial, sans-serif; margin: 0; }
                    header { background: #ffffff; border-bottom: 1px solid #d4d4d8; display: grid; gap: 6px; padding: 14px 18px; position: sticky; top: 0; z-index: 1; }
                    h1 { font-size: 20px; margin: 0; }
                    dl { display: grid; grid-template-columns: max-content minmax(0, 1fr); gap: 4px 12px; margin: 0; }
                    dt { color: #52525b; font-weight: 700; }
                    dd { margin: 0; overflow-wrap: anywhere; }
                    main { display: grid; justify-items: center; padding: 24px; }
                    img { background: #ffffff; box-shadow: 0 8px 28px rgba(0, 0, 0, 0.35); max-width: 100%%; }
                  </style>
                </head>
                <body>
                  <header>
                    <h1>Downloaded Document Text Check</h1>
                    <dl>
                      <dt>File</dt><dd>%s</dd>
                      <dt>Expected text</dt><dd>%s</dd>
                      <dt>Page</dt><dd>%s</dd>
                      <dt>Status</dt><dd>Text found on the rendered PDF page.</dd>
                    </dl>
                  </header>
                  <main>
                    <img alt="Rendered PDF page containing the expected text" width="%s" height="%s" src="data:image/png;base64,%s">
                  </main>
                </body>
                </html>
                """.formatted(
                escape(fileName), escape(expectedText), pageNumber, pageImageWidth, pageImageHeight, pageImageBase64);
    }

    private static String escape(String value) {
        return StringUtils.defaultString(value)
                          .replace("&", "&amp;")
                          .replace("<", "&lt;")
                          .replace(">", "&gt;")
                          .replace("\"", "&quot;");
    }

}
