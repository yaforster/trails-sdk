package io.github.yaforster.trails.sdk.composition;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * Adds an adapter's authentication mechanism to the SDK-owned security filter chain.
 * <p>
 * Implementations configure only authentication and its protocol-specific session or CSRF requirements. Trails
 * configures CORS, route matching, and authorization separately.
 */
@FunctionalInterface
public interface TrailsAuthenticationMechanism {

    void configure(HttpSecurity httpSecurity) throws Exception;

}
