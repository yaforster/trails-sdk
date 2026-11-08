package io.github.yaforster.trails.sdk.security;

import io.github.yaforster.trails.sdk.composition.AdapterId;
import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsBrowserRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsPersistenceRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsPrintingRegistration;
import io.github.yaforster.trails.sdk.composition.TrailsUserIdentity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.mock;

class TrailsAuthorizationTest {

    @Test
    void shouldPermitEveryOperationWithoutRoles_whenAccessIsUnrestricted() {
        TrailsAuthenticationRegistration registration = new TrailsAuthenticationRegistration(new AdapterId("none"),
                http -> {
                }, authentication -> new TrailsUserIdentity(UUID.randomUUID()),
                io.github.yaforster.trails.sdk.composition.TrailsAccessPolicy.UNRESTRICTED);
        TrailsAuthorization authorization = new TrailsAuthorization(composition(Optional.of(registration)),
                new TrailsSecurityProperties(null));

        assertThat(java.util.List.of(authorization.canManageResources(null), authorization.canAccessTestData(null),
                authorization.canExecuteTests(null), authorization.canManageUsers(null),
                authorization.canReportDeployments(null), authorization.canPromoteResources())).containsOnly(true);
    }

    @Test
    void shouldPermitResourceManagementWithoutAuthenticationAdapter() {
        TrailsAuthorization authorization = new TrailsAuthorization(composition(Optional.empty()),
                new TrailsSecurityProperties(null));

        assertThat(authorization.canManageResources(null)).isTrue();
    }

    @Test
    void shouldPermitTestExecutionForConfiguredTesterRole() {
        TrailsAuthorization authorization = new TrailsAuthorization(
                composition(Optional.of(authenticationRegistration())), roles());
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("user", "password", "tester");

        assertThat(authorization.canExecuteTests(authentication)).isTrue();
    }

    @Test
    void shouldPermitUserManagementForConfiguredAdminRole() {
        TrailsAuthorization authorization = new TrailsAuthorization(
                composition(Optional.of(authenticationRegistration())), roles());

        assertThat(authorization.canManageUsers(authenticated("admin"))).isTrue();
    }

    @Test
    void shouldPermitDeploymentReportingForConfiguredReporterRole() {
        TrailsAuthorization authorization = new TrailsAuthorization(
                composition(Optional.of(authenticationRegistration())), roles());

        assertThat(authorization.canReportDeployments(authenticated("reporter"))).isTrue();
    }

    @Test
    void shouldRejectAnonymousAuthenticationWhenAnAdapterIsRegistered() {
        TrailsAuthorization authorization = new TrailsAuthorization(
                composition(Optional.of(authenticationRegistration())), roles());
        AnonymousAuthenticationToken anonymous = new AnonymousAuthenticationToken("key", "anonymous",
                java.util.List.of(new SimpleGrantedAuthority("tester")));

        assertThat(authorization.canExecuteTests(anonymous)).isFalse();
    }

    @Test
    void shouldRejectAuthenticationAdapterWithoutEveryTrailsRole() {
        assertThatIllegalStateException().isThrownBy(() -> new TrailsAuthorization(
                composition(Optional.of(authenticationRegistration())), new TrailsSecurityProperties(null)))
                                         .withMessageContaining(
                                                 "trails.security.roles.test-manager, tester, admin, and deployment-reporter");
    }

    private TrailsSecurityProperties roles() {
        return new TrailsSecurityProperties(
                new TrailsSecurityProperties.Roles("manager", "tester", "admin", "reporter"));
    }

    private TrailsComposition composition(Optional<TrailsAuthenticationRegistration> authentication) {
        TrailsPersistenceRegistration persistence = new TrailsPersistenceRegistration(new AdapterId("persistence"),
                new TrailsPersistenceRegistration.Ports(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(),
                        mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock()));
        return new TrailsComposition(persistence, new TrailsBrowserRegistration(new AdapterId("browser"), mock()),
                new TrailsPrintingRegistration(new AdapterId("printer"), mock()), authentication, java.util.List.of());
    }

    private TrailsAuthenticationRegistration authenticationRegistration() {
        return new TrailsAuthenticationRegistration(new AdapterId("authentication"), httpSecurity -> {
        }, authentication -> new TrailsUserIdentity(UUID.randomUUID()));
    }

    private TestingAuthenticationToken authenticated(String authority) {
        return new TestingAuthenticationToken("user", "password", authority);
    }

}
