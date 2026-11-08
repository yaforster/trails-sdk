package io.github.yaforster.trails.adapter.api.rest.user;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.ScreenshotFileRequestMapper;
import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsIdentityService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires user-profile transport only when Trails has an identity-providing authentication adapter.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(TrailsAuthenticationRegistration.class)
public class UserProfileTransportConfiguration {

    @Bean
    UserProfileRequestValidator userProfileRequestValidator() {
        return new UserProfileRequestValidator();
    }

    @Bean
    UserProfileResponseAssembler userProfileResponseAssembler() {
        return new UserProfileResponseAssembler();
    }

    @Bean
    UserProfileController userProfileController(TrailsComposition composition, TrailsIdentityService identity,
            UserProfileRequestValidator requests, ScreenshotFileRequestMapper uploads,
            UserProfileResponseAssembler responses) {
        return new UserProfileController(composition.persistence()
                                                    .ports()
                                                    .userProfileService(),
                identity, requests, uploads, responses);
    }

}
