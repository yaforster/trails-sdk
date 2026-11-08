package io.github.yaforster.trails.core.user;

import io.github.yaforster.trails.core.BinaryContent;

import java.time.OffsetDateTime;

public record UserProfilePicture(BinaryContent content, String fileName, String contentType, OffsetDateTime updatedAt) {

    public long size() {
        return content.size();
    }

}
