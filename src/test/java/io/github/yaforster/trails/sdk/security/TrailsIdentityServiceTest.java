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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TrailsIdentityServiceTest {

    @Test
    void shouldReturnEmptyIdentityWithoutAuthenticatedPrincipal() {
        SecurityContextHolder.clearContext();
        TrailsIdentityService identities = new TrailsIdentityService(composition(Optional.empty()));

        assertThat(identities.currentIdentity()).isEmpty();
    }

    @Test
    void shouldResolveAuthenticatedIdentityThroughTheAuthenticationAdapter() {
        UUID userId = UUID.randomUUID();
        SecurityContextHolder.getContext()
                             .setAuthentication(new TestingAuthenticationToken("user", "password", "tester"));
        TrailsIdentityService identities = new TrailsIdentityService(
                composition(Optional.of(authenticationRegistration(userId))));

        assertThat(identities.currentIdentity()).contains(new TrailsUserIdentity(userId));
        SecurityContextHolder.clearContext();
    }

    private TrailsComposition composition(Optional<TrailsAuthenticationRegistration> authentication) {
        TrailsPersistenceRegistration persistence = new TrailsPersistenceRegistration(new AdapterId("persistence"),
                new TrailsPersistenceRegistration.Ports(mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(),
                        mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock(), mock()));
        return new TrailsComposition(persistence, new TrailsBrowserRegistration(new AdapterId("browser"), mock()),
                new TrailsPrintingRegistration(new AdapterId("printer"), mock()), authentication, java.util.List.of());
    }

    private TrailsAuthenticationRegistration authenticationRegistration(UUID userId) {
        return new TrailsAuthenticationRegistration(new AdapterId("authentication"), httpSecurity -> {
        }, authentication -> new TrailsUserIdentity(userId));
    }

}
