package io.github.yaforster.trails.sdk.composition;

import io.github.yaforster.trails.sdk.notification.Notifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates all customer and starter registrations before the SDK constructs host services.
 */
public final class TrailsCompositionValidator {

    private TrailsCompositionValidator() {}

    public static TrailsComposition validate(List<TrailsPersistenceRegistration> persistenceRegistrations,
            List<TrailsBrowserRegistration> browserRegistrations,
            List<TrailsPrintingRegistration> printingRegistrations,
            List<TrailsAuthenticationRegistration> authenticationRegistrations, List<Notifier> notifiers) {
        List<TrailsPersistenceRegistration> persistence = List.copyOf(persistenceRegistrations);
        List<TrailsBrowserRegistration> browser = List.copyOf(browserRegistrations);
        List<TrailsPrintingRegistration> printing = List.copyOf(printingRegistrations);
        List<TrailsAuthenticationRegistration> authentication = List.copyOf(authenticationRegistrations);
        List<Notifier> notifierSnapshot = List.copyOf(notifiers);

        List<String> violations = new ArrayList<>();
        requireExactlyOne("persistence", persistence.size(), violations);
        requireExactlyOne("browser", browser.size(), violations);
        requireExactlyOne("printing", printing.size(), violations);
        requireAtMostOne(authentication.size(), violations);
        requireDistinctNotifierIdentifiers(notifierSnapshot, violations);
        if (!violations.isEmpty()) {
            throw new IllegalStateException("Invalid Trails composition: " + String.join("; ", violations));
        }
        return new TrailsComposition(
                persistence.getFirst(), browser.getFirst(), printing.getFirst(), authentication.stream()
                                                                                               .findFirst(),
                notifierSnapshot);
    }

    private static void requireExactlyOne(String capability, int registrations, List<String> violations) {
        if (registrations != 1) {
            violations.add("exactly one " + capability + " registration is required, found " + registrations);
        }
    }

    private static void requireAtMostOne(int registrations, List<String> violations) {
        if (registrations > 1) {
            violations.add("at most one " + "authentication" + " registration is allowed, found " + registrations);
        }
    }

    private static void requireDistinctNotifierIdentifiers(List<Notifier> notifiers, List<String> violations) {
        Set<AdapterId> identifiers = new HashSet<>();
        for (Notifier notifier : notifiers) {
            if (!identifiers.add(notifier.adapterId())) {
                violations.add("notifier registration identifiers must be distinct");
                return;
            }
        }
    }

}
