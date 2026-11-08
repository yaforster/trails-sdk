package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsBrowserRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsPersistenceRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsPrintingRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import io.github.yaforster.trails.core.CapabilityLabel;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.mockito.Mockito.mock;

@SpringBootConfiguration(proxyBeanMethods = false)
@EnableAutoConfiguration
@Import(TrailsSecurityTestApplication.SecurityProbeController.class)
class TrailsSecurityTestApplication {

    @Bean
    PrintService printer() {
        return mock();
    }

    @Bean
    TrailsPrintingRegistration printingRegistration(PrintService printer) {
        return new TrailsPrintingRegistration(new AdapterId("test-printer"), printer);
    }

    @Bean
    ApplicationDatabaseService applicationDatabaseService() {
        return mock();
    }

    @Bean
    DeploymentDatabaseService deploymentDatabaseService() {
        return mock();
    }

    @Bean
    StageDatabaseService stageDatabaseService() {
        return mock();
    }

    @Bean
    ElementDatabaseService elementDatabaseService() {
        return mock();
    }

    @Bean
    ScreenshotDatabaseService screenshotDatabaseService() {
        return mock();
    }

    @Bean
    TestDataDatabaseService testDataDatabaseService() {
        return mock();
    }

    @Bean
    ActionQueryService actionQueryService() {
        return mock();
    }

    @Bean
    TestPlanDatabaseService testPlanDatabaseService() {
        return mock();
    }

    @Bean
    TestRunResultDatabaseService testRunResultDatabaseService() {
        return mock();
    }

    @Bean
    TestSetResultQueryService testSetResultQueryService() {
        return mock();
    }

    @Bean
    TestPathResultQueryService testPathResultQueryService() {
        return mock();
    }

    @Bean
    ActionResultQueryService actionResultQueryService() {
        return mock();
    }

    @Bean
    ActionResultScreenshotQueryService actionResultScreenshotQueryService() {
        return mock();
    }

    @Bean
    ArtifactQueryService artifactQueryService() {
        return mock();
    }

    @Bean
    UserProfileService userProfileService() {
        return mock();
    }

    @Bean
    TrailsPersistenceRegistration persistenceRegistration(ApplicationDatabaseService applicationDatabaseService,
            DeploymentDatabaseService deploymentDatabaseService, StageDatabaseService stageDatabaseService,
            ElementDatabaseService elementDatabaseService, ScreenshotDatabaseService screenshotDatabaseService,
            TestDataDatabaseService testDataDatabaseService, TestPlanDatabaseService testPlanDatabaseService,
            TestRunResultDatabaseService testRunResultDatabaseService,
            TestSetResultQueryService testSetResultQueryService, TestPathResultQueryService testPathResultQueryService,
            ActionQueryService actionQueryService, ActionResultQueryService actionResultQueryService,
            ActionResultScreenshotQueryService actionResultScreenshotQueryService,
            ArtifactQueryService artifactQueryService, UserProfileService userProfileService) {
        return new TrailsPersistenceRegistration(new AdapterId("test-persistence"),
                new TrailsPersistenceRegistration.Ports(applicationDatabaseService, deploymentDatabaseService,
                        stageDatabaseService, elementDatabaseService, screenshotDatabaseService,
                        testDataDatabaseService, testPlanDatabaseService, actionQueryService,
                        testRunResultDatabaseService, testSetResultQueryService, mock(), testPathResultQueryService,
                        mock(), actionResultQueryService, mock(), actionResultScreenshotQueryService,
                        artifactQueryService, mock(), userProfileService));
    }

    @Bean
    TrailsBrowserRegistration browserRegistration() {
        return new TrailsBrowserRegistration(new AdapterId("test-browser"), mock());
    }

    @Bean
    CapabilityContributor browserCapabilityContributor() {
        return () -> java.util.List.of(
                new CapabilityContribution(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "30000"));
    }

    @Bean
    @ConditionalOnProperty(name = "trails.test.authentication.enabled", havingValue = "true")
    TrailsAuthenticationRegistration authenticationRegistration() {
        return new TrailsAuthenticationRegistration(new AdapterId("test-authentication"),
                httpSecurity -> httpSecurity.httpBasic(Customizer.withDefaults())
                                            .csrf(AbstractHttpConfigurer::disable),
                authentication -> new TrailsUserIdentity(UUID.nameUUIDFromBytes(authentication.getName()
                                                                                              .getBytes(
                                                                                                      StandardCharsets.UTF_8))));
    }

    @Bean
    @ConditionalOnProperty(name = "trails.test.authentication.enabled", havingValue = "true")
    UserDetailsService userDetailsService() {
        return new InMemoryUserDetailsManager(User.withUsername("tester")
                                                  .password("{noop}password")
                                                  .authorities("tester")
                                                  .build(),
                User.withUsername("manager")
                    .password("{noop}password")
                    .authorities("manager")
                    .build(),
                User.withUsername("admin")
                    .password("{noop}password")
                    .authorities("admin")
                    .build(),
                User.withUsername("reporter")
                    .password("{noop}password")
                    .authorities("reporter")
                    .build());
    }

    @RestController
    static class SecurityProbeController {

        @GetMapping({"/api/security-probe", "/user/security-probe", "/security-probe"})
        void getProbe() {}

        @PostMapping("/api/security-probe")
        void postProbe() {}

        @GetMapping("/api/security-authorized-probe")
        @org.springframework.security.access.prepost.PreAuthorize("@trailsAuthorization.canExecuteTests(authentication)")
        void getAuthorizedProbe() {}

    }

}
