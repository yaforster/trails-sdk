package io.github.yaforster.trails.sdk.security;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Resolves the current request identity without exposing adapter principal types to Trails transport code.
 */
public final class TrailsIdentityService {

    private final TrailsComposition composition;

    public TrailsIdentityService(TrailsComposition composition) {
        this.composition = Objects.requireNonNull(composition, "Trails composition is required");
    }

    /**
     * Returns the resolved identity for the current authenticated request, if authentication is enabled.
     *
     * @return the current Trails identity or an empty value for unauthenticated requests
     */
    public Optional<TrailsUserIdentity> currentIdentity() {
        Authentication authentication = SecurityContextHolder.getContext()
                                                             .getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return composition.authentication()
                          .map(registration -> Objects.requireNonNull(registration.userIdentityResolver()
                                                                                  .resolve(authentication),
                                  "Authentication adapter returned no identity"));
    }

}
