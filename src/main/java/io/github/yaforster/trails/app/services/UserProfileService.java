package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePicture;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;

import java.util.Optional;
import java.util.UUID;

public interface UserProfileService {

    record UserProfileChange(UUID userId, UserProfileUpdate update) {
    }

    record UserProfilePictureChange(UUID userId, TrailsScreenshotFile file) {
    }

    record UserPage(Integer page, Integer size) {
    }

    UserProfile getOrCreateProfile(UUID userId);

    UserProfile updateProfile(UserProfileChange profileChange);

    Optional<UserProfilePicture> getProfilePicture(UUID userId);

    UserProfilePictureResource updateProfilePicture(UserProfilePictureChange pictureChange);

    void deleteProfilePicture(UUID userId);

    void deleteUserData(UUID userId);

    PagedResult<UserProfile> listUsers(UserPage userPage);

}
