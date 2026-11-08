package io.github.yaforster.trails.sdk.host.capability;

import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.Capability;
import io.github.yaforster.trails.core.CapabilityLabel;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class SdkCapabilitiesServiceTest {

    @Test
    void shouldUseCanonicalLabelOrder_whenContributorsUseAnotherOrder() {
        SdkCapabilitiesService service = new SdkCapabilitiesService(
                List.of(() -> List.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"),
                        new CapabilityContribution(CapabilityLabel.TEST_PLAN_MAXIMUM_ACTIONS, "10"),
                        new CapabilityContribution(CapabilityLabel.TEST_PLAN_MAXIMUM_PATHS, "20"),
                        new CapabilityContribution(CapabilityLabel.TEST_PLAN_MAXIMUM_ACTION_OCCURRENCES, "30"),
                        new CapabilityContribution(CapabilityLabel.AUTHENTICATION_REQUIRED, "true"),
                        new CapabilityContribution(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "123"),
                        new CapabilityContribution(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, "PNG"),
                        new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES, "3"),
                        new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_WIDTH, "2"),
                        new CapabilityContribution(CapabilityLabel.USER_IMAGE_MAX_HEIGHT, "1"))));

        assertThat(service.capabilities()).containsExactly(new Capability(CapabilityLabel.USER_IMAGE_MAX_HEIGHT, "1"),
                new Capability(CapabilityLabel.USER_IMAGE_MAX_WIDTH, "2"),
                new Capability(CapabilityLabel.USER_IMAGE_MAX_SIZE_BYTES, "3"),
                new Capability(CapabilityLabel.SUPPORTED_IMAGE_FORMATS, "PNG"),
                new Capability(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "123"),
                new Capability(CapabilityLabel.USER_FEATURES_ACTIVE, "true"),
                new Capability(CapabilityLabel.AUTHENTICATION_REQUIRED, "true"),
                new Capability(CapabilityLabel.TEST_PLAN_MAXIMUM_ACTIONS, "10"),
                new Capability(CapabilityLabel.TEST_PLAN_MAXIMUM_PATHS, "20"),
                new Capability(CapabilityLabel.TEST_PLAN_MAXIMUM_ACTION_OCCURRENCES, "30"));
    }

    @Test
    void shouldRejectMissingCapabilityLabels() {
        CapabilityContributor incomplete = () -> List.of(
                new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"));

        assertThatIllegalStateException().isThrownBy(() -> new SdkCapabilitiesService(List.of(incomplete)))
                                         .withMessageContaining("Missing capability labels");
    }

    @Test
    void shouldRejectDuplicateCapabilityLabels() {
        CapabilityContributor first = () -> List.of(
                new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "true"));
        CapabilityContributor second = () -> List.of(
                new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, "false"));

        assertThatIllegalStateException().isThrownBy(() -> new SdkCapabilitiesService(List.of(first, second)))
                                         .withMessageContaining("Duplicate capability labels");
    }

}
