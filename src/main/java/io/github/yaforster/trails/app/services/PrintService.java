package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.print.TestPathPrintContext;
import io.github.yaforster.trails.core.print.TestRunPrintContext;
import io.github.yaforster.trails.core.print.TestSetPrintContext;

import java.io.IOException;
import java.io.OutputStream;

public interface PrintService {

    void renderTestPathPdf(TestPathPdf testPathPdf) throws IOException;

    void renderTestSetPdf(TestSetPdf testSetPdf) throws IOException;

    void renderTestRunPdf(TestRunPdf testRunPdf) throws IOException;

    record TestPathPdf(TestPathPrintContext testPathPrintContext, OutputStream outputStream) {
    }

    record TestSetPdf(TestSetPrintContext testSetPrintContext, OutputStream outputStream) {
    }

    record TestRunPdf(TestRunPrintContext testRunPrintContext, OutputStream outputStream) {
    }

}
