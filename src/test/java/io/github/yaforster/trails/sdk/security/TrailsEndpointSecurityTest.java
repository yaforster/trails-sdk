package io.github.yaforster.trails.sdk.security;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrailsEndpointSecurityTest {
    @ParameterizedTest
    @ValueSource(strings = {"/api", "/user", "/api/mcp", "/user/mcp", "/mcp/**", "/mcp/", "/", "/mcp?x=1", "mcp"})
    void shouldRejectAmbiguousOrReservedPaths_whenRegisteringTransport(String path) {
        assertThatThrownBy(
                () -> new TrailsEndpointSecurity(path, authentication -> authentication, request -> null)).isInstanceOf(
                        IllegalArgumentException.class);
    }
}
