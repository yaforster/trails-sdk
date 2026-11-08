package io.github.yaforster.trails.core.user;

import java.util.Optional;

public record UserProfileUpdate(Optional<String> firstName, Optional<String> lastName, Optional<String> email,
        Optional<String> phoneNumber) {
}
