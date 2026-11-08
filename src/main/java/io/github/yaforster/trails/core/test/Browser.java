package io.github.yaforster.trails.core.test;

import java.util.Locale;

public enum Browser {

    EDGE,
    FIREFOX,
    CHROME;

    public static Browser parse(String browserName) {
        if (browserName == null || browserName.isBlank()) {
            throw new IllegalArgumentException("Browser must not be null or blank");
        }
        try {
            return Browser.valueOf(browserName.trim()
                                              .toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unsupported browser value: " + browserName, ex);
        }
    }

}
