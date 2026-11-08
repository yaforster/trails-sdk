package io.github.yaforster.trails.sdk.composition;

import java.util.Objects;
import java.util.UUID;

/**
 * The stable Trails identity associated with an authenticated principal.
 *
 * @param userId
 *            stable user identifier supplied by the authentication adapter
 */
public record TrailsUserIdentity(UUID userId) {

    public TrailsUserIdentity {
        Objects.requireNonNull(userId, "User identifier is required");
    }

}
