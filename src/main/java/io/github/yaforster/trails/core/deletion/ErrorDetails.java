package io.github.yaforster.trails.core.deletion;

import java.io.PrintWriter;
import java.io.StringWriter;

public record ErrorDetails(String type, String message, String stackTrace) {

    public static ErrorDetails fromException(Throwable ex) {
        return new ErrorDetails(ex.getClass()
                                  .getSimpleName(),
                ex.getMessage(), stackTraceToString(ex));
    }

    private static String stackTraceToString(Throwable ex) {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
