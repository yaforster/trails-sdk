package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.CapabilitiesService;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.app.services.CapabilityContribution;
import io.github.yaforster.trails.app.services.CapabilityContributor;
import io.github.yaforster.trails.core.CapabilityLabel;
import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsBrowserRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsPersistenceRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsPrintingRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import io.github.yaforster.trails.sdk.notification.Notifier;
import io.github.yaforster.trails.sdk.notification.NotifierDispatcher;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TrailsSdkAutoConfigurationTest {

    @Test
    void shouldStartWhenExactlyOneRequiredRegistrationIsPresent() {
        contextRunner().withUserConfiguration(CompleteComposition.class)
                       .run(context -> {
                           assertThat(context).hasNotFailed();
                           assertThat(context).hasSingleBean(TrailsComposition.class);
                           assertThat(context).hasSingleBean(NotifierDispatcher.class);
                           assertThat(context).hasSingleBean(TestService.class);
                           assertThat(context).hasSingleBean(TestExecutionFacadeService.class);
                           assertThat(context).hasSingleBean(TestExecutionEventPublisher.class);
                           assertThat(context).hasSingleBean(TestExecutionEventHub.class);
                           assertThat(context).hasSingleBean(CapabilitiesService.class);
                       });
    }

    @Test
    void shouldRejectIncompleteCapabilityMetadata() {
        contextRunner().withUserConfiguration(CompositionWithoutBrowserCapability.class)
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasRootCauseMessage(
                                   "Missing capability labels: [FILE_DOWNLOAD_TIMEOUT_MILLIS]");
                       });
    }

    @Test
    void shouldRejectMissingRequiredRegistrations() {
        contextRunner().run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure())
                                                   .hasMessageContaining(
                                                           "exactly one persistence registration is required, found 0")
                                                   .hasMessageContaining(
                                                           "exactly one browser registration is required, found 0");
        });
    }

    @Test
    void shouldRejectMultiplePersistenceRegistrations() {
        contextRunner().withUserConfiguration(DuplicatePersistenceComposition.class)
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasMessageContaining(
                                   "exactly one persistence registration is required, found 2");
                       });
    }

    @Test
    void shouldRejectMultipleAuthenticationRegistrations() {
        contextRunner().withUserConfiguration(DuplicateAuthenticationComposition.class)
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasMessageContaining(
                                   "at most one authentication registration is allowed, found 2");
                       });
    }

    @Test
    void shouldRejectAuthenticationRegistrationWithoutEveryTrailsRole() {
        contextRunner().withUserConfiguration(AuthenticationWithoutRolesComposition.class)
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasMessageContaining(
                                   "trails.security.roles.test-manager, tester, admin, and deployment-reporter");
                       });
    }

    @Test
    void shouldRejectCustomerSecurityFilterChain() {
        new org.springframework.boot.test.context.runner.WebApplicationContextRunner().withUserConfiguration(
                TrailsSecurityTestApplication.class, CustomerSecurityConfiguration.class)
                                                                                      .run(context -> {
                                                                                          assertThat(
                                                                                                  context).hasFailed();
                                                                                          assertThat(
                                                                                                  context.getStartupFailure()).hasMessageContainingAll(
                                                                                                          "Customer SecurityFilterChain beans are not supported",
                                                                                                          "TrailsAuthenticationRegistration",
                                                                                                          "TrailsEndpointSecurity");
                                                                                      });
    }

    @Test
    void shouldRejectDuplicateNotifierIdentifiers() {
        contextRunner().withUserConfiguration(DuplicateNotifierComposition.class)
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasMessageContaining(
                                   "notifier registration identifiers must be distinct");
                       });
    }

    @Test
    void shouldBindNotifierDispatchLimits() {
        contextRunner().withUserConfiguration(CompleteComposition.class)
                       .withPropertyValues("trails.notification.dispatch.worker-count=2",
                               "trails.notification.dispatch.queue-capacity=3",
                               "trails.notification.dispatch.shutdown-timeout=5s")
                       .run(context -> {
                           assertThat(context).hasNotFailed();
                           assertThat(context.getBean(NotifierDispatchProperties.class)).isEqualTo(
                                   new NotifierDispatchProperties(2, 3, java.time.Duration.ofSeconds(5)));
                       });
    }

    @Test
    void shouldRejectInvalidNotifierDispatchLimits() {
        contextRunner().withUserConfiguration(CompleteComposition.class)
                       .withPropertyValues("trails.notification.dispatch.worker-count=-1")
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasRootCauseMessage(
                                   "Notifier worker count must be positive");
                       });
    }

    @Test
    void shouldBindTestExecutionLimits() {
        contextRunner().withUserConfiguration(CompleteComposition.class)
                       .withPropertyValues("trails.execution.maximum-concurrent-runs=2",
                               "trails.execution.run-queue-capacity=0",
                               "trails.execution.maximum-concurrent-test-sets=3",
                               "trails.execution.shutdown-timeout=5s")
                       .run(context -> {
                           assertThat(context).hasNotFailed();
                           assertThat(context.getBean(TestExecutionProperties.class)).isEqualTo(
                                   new TestExecutionProperties(2, 0, 3, java.time.Duration.ofSeconds(5)));
                       });
    }

    @Test
    void shouldRejectInvalidTestExecutionLimits() {
        contextRunner().withUserConfiguration(CompleteComposition.class)
                       .withPropertyValues("trails.execution.maximum-concurrent-runs=0")
                       .run(context -> {
                           assertThat(context).hasFailed();
                           assertThat(context.getStartupFailure()).hasRootCauseMessage(
                                   "Maximum concurrent test runs must be positive");
                       });
    }

    private static ApplicationContextRunner contextRunner() {
        return new ApplicationContextRunner().withUserConfiguration(BootApplication.class);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    static class BootApplication {

    }

    @Configuration(proxyBeanMethods = false)
    static class CompleteComposition {

        @Bean
        @ConditionalOnProperty(name = "test.printer", matchIfMissing = true)
        TrailsPrintingRegistration printer() {
            return new TrailsPrintingRegistration(new AdapterId("test-printer"), mock());
        }

        @Bean
        TrailsPersistenceRegistration persistenceRegistration() {
            return TrailsSdkAutoConfigurationTest.persistenceRegistration("test-persistence");
        }

        @Bean
        TrailsBrowserRegistration browserRegistration() {
            return TrailsSdkAutoConfigurationTest.browserRegistration("test-browser");
        }

        @Bean
        CapabilityContributor browserCapabilityContributor() {
            return () -> java.util.List.of(
                    new CapabilityContribution(CapabilityLabel.FILE_DOWNLOAD_TIMEOUT_MILLIS, "30000"));
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CompositionWithoutBrowserCapability {

        @Bean
        TrailsPrintingRegistration printer() {
            return new TrailsPrintingRegistration(new AdapterId("test-printer"), mock());
        }

        @Bean
        TrailsPersistenceRegistration persistenceRegistration() {
            return TrailsSdkAutoConfigurationTest.persistenceRegistration("test-persistence");
        }

        @Bean
        TrailsBrowserRegistration browserRegistration() {
            return TrailsSdkAutoConfigurationTest.browserRegistration("test-browser");
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class DuplicatePersistenceComposition extends CompleteComposition {

        @Bean
        TrailsPersistenceRegistration otherPersistenceRegistration() {
            return TrailsSdkAutoConfigurationTest.persistenceRegistration("other-persistence");
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class DuplicateAuthenticationComposition extends CompleteComposition {

        @Bean
        TrailsAuthenticationRegistration firstAuthenticationRegistration() {
            return TrailsSdkAutoConfigurationTest.authenticationRegistration("first-authentication");
        }

        @Bean
        TrailsAuthenticationRegistration secondAuthenticationRegistration() {
            return TrailsSdkAutoConfigurationTest.authenticationRegistration("second-authentication");
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class AuthenticationWithoutRolesComposition extends CompleteComposition {

        @Bean
        TrailsAuthenticationRegistration authenticationRegistration() {
            return TrailsSdkAutoConfigurationTest.authenticationRegistration("authentication");
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class DuplicateNotifierComposition extends CompleteComposition {

        @Bean
        Notifier firstNotifier() {
            return new TestNotifier("test-notifier");
        }

        @Bean
        Notifier secondNotifier() {
            return new TestNotifier("test-notifier");
        }

    }

    @Configuration(proxyBeanMethods = false)
    static class CustomerSecurityConfiguration {

        @Bean
        SecurityFilterChain customerSecurityFilterChain(HttpSecurity httpSecurity) throws Exception {
            httpSecurity.authorizeHttpRequests(authorize -> authorize.anyRequest()
                                                                     .permitAll());
            return httpSecurity.build();
        }

    }

    private static TrailsPersistenceRegistration persistenceRegistration(String adapterId) {
        return new TrailsPersistenceRegistration(new AdapterId(adapterId),
                new TrailsPersistenceRegistration.Ports(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(),
                        mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock()));
    }

    private static TrailsBrowserRegistration browserRegistration(String adapterId) {
        return new TrailsBrowserRegistration(new AdapterId(adapterId), mock());
    }

    private static TrailsAuthenticationRegistration authenticationRegistration(String adapterId) {
        return new TrailsAuthenticationRegistration(new AdapterId(adapterId), httpSecurity -> {
        }, authentication -> new TrailsUserIdentity(java.util.UUID.randomUUID()));
    }

}
