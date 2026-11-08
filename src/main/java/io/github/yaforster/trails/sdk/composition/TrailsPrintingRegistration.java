package io.github.yaforster.trails.sdk.composition;

import io.github.yaforster.trails.app.services.PrintService;
import java.util.Objects;

/**
 * Registers one complete printing implementation for path, browser-result, and test-run reports.
 *
 * @param adapterId
 *            identity of the customer-selected printing adapter
 * @param printService
 *            renderer for all supported report levels
 */
public record TrailsPrintingRegistration(AdapterId adapterId, PrintService printService) {

    public TrailsPrintingRegistration {
        Objects.requireNonNull(adapterId, "Printing adapter identifier is required");
        Objects.requireNonNull(printService, "Print service is required");
    }
}
