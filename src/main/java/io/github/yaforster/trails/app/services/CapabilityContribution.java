package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.CapabilityLabel;

import java.util.Objects;

public record CapabilityContribution(CapabilityLabel label, String value) {

    public CapabilityContribution {
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(value, "value must not be null");
    }

}
