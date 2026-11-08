package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.adapter.api.rest.model.PagedUserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ProfilePictureResourceDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.TrailsScreenshotFile;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePicture;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import io.github.yaforster.trails.sdk.security.TrailsIdentityService;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;
import org.openapitools.api.UserApi;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** Exposes authenticated user profiles through the validated persistence composition. */
@RestController
public class UserProfileController implements UserApi {

    private final UserProfileService profiles;
    private final TrailsIdentityService identity;
    private final UserProfileRequestValidator requests;
    private final ScreenshotFileRequestMapper uploads;
    private final UserProfileResponseAssembler responses;

    UserProfileController(UserProfileService profiles, TrailsIdentityService identity,
            UserProfileRequestValidator requests, ScreenshotFileRequestMapper uploads,
            UserProfileResponseAssembler responses) {
        this.profiles = Objects.requireNonNull(profiles, "User profile service is required");
        this.identity = Objects.requireNonNull(identity, "Trails identity service is required");
        this.requests = Objects.requireNonNull(requests, "User profile request validator is required");
        this.uploads = Objects.requireNonNull(uploads, "Screenshot upload mapper is required");
        this.responses = Objects.requireNonNull(responses, "User profile response assembler is required");
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageUsers(authentication)")
    public ResponseEntity<PagedUserProfileDTO> listUsers(Integer page, Integer size) {
        requests.validatePage(page, size);
        PagedResult<UserProfile> profilesPage = profiles.listUsers(new UserProfileService.UserPage(page, size));
        return ResponseEntity.ok(responses.profiles(profilesPage));
    }

    @Override
    public ResponseEntity<UserProfileDTO> getProfile() {
        return ResponseEntity.ok(responses.profile(profiles.getOrCreateProfile(currentUserId())));
    }

    @Override
    public ResponseEntity<Resource> getProfilePicture() {
        return profiles.getProfilePicture(currentUserId())
                       .map(this::profilePictureResponse)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    public ResponseEntity<UserProfileDTO> updateProfile(UserProfileUpdateDTO update) {
        requests.validateProfileUpdate(update);
        UserProfile profile = profiles.updateProfile(
                new UserProfileService.UserProfileChange(currentUserId(), responses.profileUpdate(update)));
        return ResponseEntity.ok(responses.profile(profile));
    }

    @Override
    public ResponseEntity<ProfilePictureResourceDTO> updateProfilePicture(MultipartFile file) {
        requests.validateProfilePicture(file);
        TrailsScreenshotFile picture = uploads.from(file);
        UserProfilePictureResource resource = profiles.updateProfilePicture(
                new UserProfileService.UserProfilePictureChange(currentUserId(), picture));
        return ResponseEntity.ok(responses.profilePicture(resource));
    }

    @Override
    public ResponseEntity<Void> deleteProfilePicture() {
        profiles.deleteProfilePicture(currentUserId());
        return ResponseEntity.noContent()
                             .build();
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageUsers(authentication)")
    public ResponseEntity<Void> deleteUserData(UUID userId) {
        profiles.deleteUserData(userId);
        return ResponseEntity.noContent()
                             .build();
    }

    private ResponseEntity<Resource> profilePictureResponse(UserProfilePicture picture) {
        byte[] content = picture.content()
                                .bytes();
        String filename = profilePictureFilename(picture.fileName());
        String disposition = ContentDisposition.inline()
                                               .filename(filename, StandardCharsets.UTF_8)
                                               .build()
                                               .toString();
        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(picture.contentType()))
                             .contentLength(content.length)
                             .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                             .body(new ByteArrayResource(content));
    }

    private UUID currentUserId() {
        return identity.currentIdentity()
                       .map(TrailsUserIdentity::userId)
                       .orElseThrow(
                               () -> new AuthorizationDeniedException("An authenticated Trails identity is required"));
    }

    private String profilePictureFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "profile-picture";
        }
        return filename;
    }

}
