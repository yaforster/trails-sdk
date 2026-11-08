package io.github.yaforster.trails.core;

/**
 * Capabilities communicate configured limits and supported data types to the client such as the maximum allowed
 * screenshot sizes. They're meant to be used via the capabilities-endpoint and checked by the client.
 */
public record Capability(CapabilityLabel label, String description, String value) {

    public Capability(CapabilityLabel capabilityLabel, String value) {
        this(capabilityLabel, capabilityLabel.getDescription(), value);
    }
}
