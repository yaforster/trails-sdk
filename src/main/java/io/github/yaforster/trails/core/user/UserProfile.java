package io.github.yaforster.trails.core.user;

import java.util.UUID;

public record UserProfile(UUID userId, String firstName, String lastName, String email, String phoneNumber,
        boolean hasProfilePicture) {
}
