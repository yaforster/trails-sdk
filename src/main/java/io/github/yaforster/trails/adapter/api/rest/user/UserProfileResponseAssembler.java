package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedUserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ProfileLinksDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ProfilePictureResourceDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileDTO;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.user.UserProfile;
import io.github.yaforster.trails.core.user.UserProfilePictureResource;
import io.github.yaforster.trails.core.user.UserProfileUpdate;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpMethod;

/** Maps user-profile outcomes to the existing profile resource contract. */
final class UserProfileResponseAssembler {

    UserProfileDTO profile(UserProfile profile) {
        Objects.requireNonNull(profile, "User profile is required");
        return new UserProfileDTO().userId(profile.userId())
                                   .firstName(profile.firstName())
                                   .lastName(profile.lastName())
                                   .email(profile.email())
                                   .phoneNumber(profile.phoneNumber())
                                   .links(profileLinks(profile.hasProfilePicture()));
    }

    PagedUserProfileDTO profiles(PagedResult<UserProfile> page) {
        Objects.requireNonNull(page, "User profile page is required");
        return new PagedUserProfileDTO().page(page.page())
                                        .size(page.size())
                                        .totalElements(Math.toIntExact(page.totalItems()))
                                        .totalPages(page.totalPages())
                                        .items(page.items()
                                                   .stream()
                                                   .map(this::adminProfile)
                                                   .toList())
                                        .links(pagingLinks(page));
    }

    UserProfileUpdate profileUpdate(UserProfileUpdateDTO update) {
        Objects.requireNonNull(update, "User profile update is required");
        return new UserProfileUpdate(value(update.getFirstName()), value(update.getLastName()),
                value(update.getEmail()), value(update.getPhoneNumber()));
    }

    ProfilePictureResourceDTO profilePicture(UserProfilePictureResource resource) {
        Objects.requireNonNull(resource, "User profile picture resource is required");
        return new ProfilePictureResourceDTO().href(URI.create(avatarUri()))
                                              .contentType(resource.contentType())
                                              .updatedAt(resource.updatedAt());
    }

    private UserProfileDTO adminProfile(UserProfile profile) {
        return new UserProfileDTO().userId(profile.userId())
                                   .firstName(profile.firstName())
                                   .lastName(profile.lastName())
                                   .email(profile.email())
                                   .phoneNumber(profile.phoneNumber())
                                   .links(new ProfileLinksDTO(
                                           link(userUri(profile.userId()), HttpMethod.DELETE)).deleteUserData(
                                                   link(userUri(profile.userId()), HttpMethod.DELETE)));
    }

    private ProfileLinksDTO profileLinks(boolean hasProfilePicture) {
        ProfileLinksDTO links = new ProfileLinksDTO(link(profileUri(), HttpMethod.GET))
                                                                                       .update(link(profileUri(),
                                                                                               HttpMethod.PUT))
                                                                                       .uploadProfilePicture(
                                                                                               link(avatarUri(),
                                                                                                       HttpMethod.PUT));
        if (hasProfilePicture) {
            links.getProfilePicture(link(avatarUri(), HttpMethod.GET))
                 .deleteProfilePicture(link(avatarUri(), HttpMethod.DELETE));
        }
        return links;
    }

    private Map<String, LinkDTO> pagingLinks(PagedResult<UserProfile> page) {
        return Map.copyOf(PaginationLinks.forPage(page, pageNumber -> profilesUri(pageNumber, page.size())));
    }

    private LinkDTO link(String href, HttpMethod method) {
        return new LinkDTO().href(href)
                            .method(method.name())
                            .templated(false);
    }

    private Optional<String> value(String value) {
        return Optional.ofNullable(value);
    }

    private String profilesUri(int page, int size) {
        return "/user/profiles?page=" + page + "&size=" + size;
    }

    private String profileUri() {
        return "/user/profile";
    }

    private String avatarUri() {
        return profileUri() + "/avatar";
    }

    private String userUri(UUID userId) {
        return profileUri() + "/" + userId;
    }

}
