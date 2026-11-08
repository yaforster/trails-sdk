package io.github.yaforster.trails.sdk.composition;

import io.github.yaforster.trails.app.services.WebdriverConfigurationFactory;
import java.util.Objects;

/**
 * Registers one complete browser implementation.
 * <p>
 * {@link WebdriverConfigurationFactory} is the only host-facing browser port. It creates every browser execution,
 * including the browser session, driver provider, downloads, and document executor for a requested test run.
 */
public record TrailsBrowserRegistration(AdapterId adapterId,
        WebdriverConfigurationFactory webdriverConfigurationFactory) {

    public TrailsBrowserRegistration {
        Objects.requireNonNull(adapterId, "Browser adapter identifier is required");
        Objects.requireNonNull(webdriverConfigurationFactory, "Browser configuration factory is required");
    }

}
