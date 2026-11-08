package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.model.UserProfileUpdateDTO;
import io.github.yaforster.trails.core.ValidationViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.web.multipart.MultipartFile;

/** Validates user-profile requests before they reach the application port. */
final class UserProfileRequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    void validatePage(Integer page, Integer size) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (page == null) {
            violations.add(new ValidationViolation("USER_PAGE_NULL", "Page must not be null.", "/page"));
        } else if (page < 0) {
            violations.add(new ValidationViolation("USER_PAGE_OUT_OF_RANGE", "Page must be greater than or equal to 0.",
                    "/page"));
        }
        if (size == null) {
            violations.add(new ValidationViolation("USER_SIZE_NULL", "Size must not be null.", "/size"));
        } else if (size < 1 || size > 100) {
            violations.add(
                    new ValidationViolation("USER_SIZE_OUT_OF_RANGE", "Size must be between 1 and 100.", "/size"));
        }
        rejectIfInvalid(violations);
    }

    void validateProfileUpdate(UserProfileUpdateDTO update) {
        List<ValidationViolation> violations = new ArrayList<>();
        if (update == null) {
            violations.add(
                    new ValidationViolation("USER_PROFILE_UPDATE_NULL", "User profile update must not be null.", "/"));
        } else {
            validateMaximumLength(violations, update.getFirstName(), 100, "USER_FIRST_NAME_TOO_LONG",
                    "First name must not be longer than 100 characters.", "/firstName");
            validateMaximumLength(violations, update.getLastName(), 100, "USER_LAST_NAME_TOO_LONG",
                    "Last name must not be longer than 100 characters.", "/lastName");
            validateMaximumLength(violations, update.getEmail(), 255, "USER_EMAIL_TOO_LONG",
                    "Email must not be longer than 255 characters.", "/email");
            if (isInvalidEmail(update.getEmail())) {
                violations.add(new ValidationViolation("USER_EMAIL_INVALID", "Email must be a valid email address.",
                        "/email"));
            }
            validateMaximumLength(violations, update.getPhoneNumber(), 50, "USER_PHONE_NUMBER_TOO_LONG",
                    "Phone number must not be longer than 50 characters.", "/phoneNumber");
        }
        rejectIfInvalid(violations);
    }

    void validateProfilePicture(MultipartFile file) {
        if (file == null) {
            throw new ApiRequestValidationException(List.of(new ValidationViolation("USER_PROFILE_PICTURE_FILE_NULL",
                    "User profile picture file must not be null.", "/file")));
        }
    }

    private void validateMaximumLength(List<ValidationViolation> violations, String value, int maximumLength,
            String code, String message, String path) {
        if (value != null && value.length() > maximumLength) {
            violations.add(new ValidationViolation(code, message, path));
        }
    }

    private boolean isInvalidEmail(String email) {
        return email != null && !email.isBlank() && !EMAIL_PATTERN.matcher(email)
                                                                  .matches();
    }

    private void rejectIfInvalid(List<ValidationViolation> violations) {
        if (!violations.isEmpty()) {
            throw new ApiRequestValidationException(violations);
        }
    }

}
