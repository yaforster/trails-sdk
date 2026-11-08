package io.github.yaforster.trails.core;

import lombok.Getter;

@Getter
public enum CapabilityLabel {

    USER_IMAGE_MAX_HEIGHT("The maximum supported pixel height of uploaded images"),
    USER_IMAGE_MAX_WIDTH("The maximum supported pixel width of uploaded images"),
    USER_IMAGE_MAX_SIZE_BYTES("The maximum file size of uploaded images in bytes"),
    SUPPORTED_IMAGE_FORMATS("The image formats supported for uploads"),
    FILE_DOWNLOAD_TIMEOUT_MILLIS("The timeout for file downloads in milliseconds"),
    USER_FEATURES_ACTIVE("Whether user profile features are active"),
    AUTHENTICATION_REQUIRED("Whether clients must authenticate before accessing Trails operations"),
    TEST_PLAN_MAXIMUM_ACTIONS("The maximum number of actions in a test-plan graph"),
    TEST_PLAN_MAXIMUM_PATHS("The maximum number of root-to-leaf execution paths per test-plan graph"),
    TEST_PLAN_MAXIMUM_ACTION_OCCURRENCES(
            "The maximum total action occurrences across all execution paths per test-plan graph");

    private final String description;

    CapabilityLabel(String description) {
        this.description = description;
    }

}
