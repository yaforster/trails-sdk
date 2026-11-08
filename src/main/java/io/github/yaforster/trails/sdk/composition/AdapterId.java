package io.github.yaforster.trails.sdk.composition;

import java.util.Objects;
import java.util.regex.Pattern;

/** A stable, diagnostic-safe identifier for one configured adapter capability. */
public record AdapterId(String value) {

    private static final Pattern VALID_VALUE = Pattern.compile("[a-z][a-z0-9-]{0,62}");

    public AdapterId {
        Objects.requireNonNull(value, "Adapter identifier is required");
        if (!VALID_VALUE.matcher(value)
                        .matches()) {
            throw new IllegalArgumentException("Adapter identifier must be lowercase kebab-case");
        }
    }

}
