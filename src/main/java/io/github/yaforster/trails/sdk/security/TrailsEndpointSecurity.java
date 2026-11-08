package io.github.yaforster.trails.sdk.security;

import java.util.Objects;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationConverter;

/**
 * Independent credentials for one exact, top-level transport endpoint. The SDK owns the filter chain; endpoint
 * credentials never enter the REST/user authentication mechanism.
 *
 * @param path
 *            exact endpoint path, outside the reserved API and user namespaces
 * @param authenticationManager
 *            verifies endpoint credentials
 * @param authenticationConverter
 *            extracts credentials from each request
 */
public record TrailsEndpointSecurity(String path, AuthenticationManager authenticationManager,
        AuthenticationConverter authenticationConverter) {
    public TrailsEndpointSecurity {
        if (path == null || !path.matches("/[a-z][a-z0-9-]*") || path.equals("/api") || path.equals("/user")) {
            throw new IllegalArgumentException("Transport security requires a non-reserved, exact top-level path");
        }
        Objects.requireNonNull(authenticationManager, "Endpoint authentication manager is required");
        Objects.requireNonNull(authenticationConverter, "Endpoint authentication converter is required");
    }
}
