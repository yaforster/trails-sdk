package io.github.yaforster.trails.sdk.composition;

import java.util.Objects;

/**
 * Registers one authentication mechanism and its mapping to stable Trails identities.
 *
 * @param adapterId
 *            stable identifier of the authentication adapter
 * @param mechanism
 *            authentication mechanism contributed by the adapter
 * @param userIdentityResolver
 *            mapping from authenticated principals to Trails identities
 * @param accessPolicy
 *            SDK authorization policy for this registration
 */
public record TrailsAuthenticationRegistration(AdapterId adapterId, TrailsAuthenticationMechanism mechanism,
        TrailsUserIdentityResolver userIdentityResolver, TrailsAccessPolicy accessPolicy) {

    /**
     * Registers a role-based provider, including existing OIDC and customer adapters.
     *
     * @param adapterId
     *            provider identifier
     * @param mechanism
     *            authentication protocol
     * @param userIdentityResolver
     *            stable identity mapping
     */
    public TrailsAuthenticationRegistration(AdapterId adapterId, TrailsAuthenticationMechanism mechanism,
            TrailsUserIdentityResolver userIdentityResolver) {
        this(adapterId, mechanism, userIdentityResolver, TrailsAccessPolicy.ROLE_BASED);
    }

    public TrailsAuthenticationRegistration {
        Objects.requireNonNull(adapterId, "Authentication adapter identifier is required");
        Objects.requireNonNull(mechanism, "Authentication mechanism is required");
        Objects.requireNonNull(userIdentityResolver, "User identity resolver is required");
        Objects.requireNonNull(accessPolicy, "Access policy is required");
    }

}
