package io.github.yaforster.trails.sdk.composition;

import io.github.yaforster.trails.sdk.notification.Notifier;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * The validated adapter snapshot consumed by every SDK host service.
 *
 * @param persistence
 *            complete persistence capability
 * @param browser
 *            complete browser capability
 * @param printing
 *            complete printing capability
 * @param authentication
 *            optional authentication capability
 * @param notifiers
 *            terminal notification providers
 */
public record TrailsComposition(TrailsPersistenceRegistration persistence, TrailsBrowserRegistration browser,
        TrailsPrintingRegistration printing, Optional<TrailsAuthenticationRegistration> authentication,
        List<Notifier> notifiers) {

    public TrailsComposition {
        Objects.requireNonNull(persistence, "Persistence registration is required");
        Objects.requireNonNull(browser, "Browser registration is required");
        Objects.requireNonNull(printing, "Printing registration is required");
        authentication = Objects.requireNonNull(authentication, "Authentication registration is required");
        notifiers = List.copyOf(Objects.requireNonNull(notifiers, "Notifiers are required"));
    }

    /** @return whether callers must supply credentials and satisfy Trails role checks */
    public boolean authenticationRequired() {
        return authentication.map(registration -> registration.accessPolicy() == TrailsAccessPolicy.ROLE_BASED)
                             .orElse(false);
    }

}
