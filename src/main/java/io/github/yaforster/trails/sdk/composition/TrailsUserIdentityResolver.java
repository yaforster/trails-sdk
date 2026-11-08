package io.github.yaforster.trails.sdk.composition;

import org.springframework.security.core.Authentication;

/** Maps every authenticated adapter principal to its stable Trails UUID identity. */
@FunctionalInterface
public interface TrailsUserIdentityResolver {

    TrailsUserIdentity resolve(Authentication authentication);

}
