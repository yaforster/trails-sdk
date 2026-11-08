package io.github.yaforster.trails.core.user;

import java.time.OffsetDateTime;

public record UserProfilePictureResource(String contentType, OffsetDateTime updatedAt) {
}
