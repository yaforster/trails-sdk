package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.security.TrailsEndpointSecurity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;

/** Builds the SDK-owned isolated transport credential boundary. */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
class TrailsEndpointSecurityConfiguration {
    static final String ENDPOINT_SECURITY_FILTER_CHAIN_BEAN_NAME = "trailsEndpointSecurityFilterChain";
    private static final HttpStatusEntryPoint UNAUTHORIZED = new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED);

    @Bean(name = ENDPOINT_SECURITY_FILTER_CHAIN_BEAN_NAME)
    @Order(0)
    SecurityFilterChain trailsEndpointSecurityFilterChain(HttpSecurity http, List<TrailsEndpointSecurity> endpoints) {
        Map<String, TrailsEndpointSecurity> endpointsByPath = indexEndpoints(endpoints);
        AuthenticationFilter authentication = endpointAuthenticationFilter(endpointsByPath);
        configureEndpointChain(http, endpointsByPath, authentication);
        return http.build();
    }

    private Map<String, TrailsEndpointSecurity> indexEndpoints(List<TrailsEndpointSecurity> endpoints) {
        Map<String, TrailsEndpointSecurity> endpointsByPath = new HashMap<>();
        for (TrailsEndpointSecurity endpoint : endpoints) {
            if (endpointsByPath.putIfAbsent(endpoint.path(), endpoint) != null) {
                throw new IllegalStateException("Duplicate transport security endpoint: " + endpoint.path());
            }
        }
        return Map.copyOf(endpointsByPath);
    }

    private AuthenticationFilter endpointAuthenticationFilter(Map<String, TrailsEndpointSecurity> endpointsByPath) {
        AuthenticationManagerResolver<HttpServletRequest> managers = request -> endpointFor(request,
                endpointsByPath).authenticationManager();
        AuthenticationFilter authentication = new AuthenticationFilter(managers,
                request -> endpointFor(request, endpointsByPath).authenticationConverter()
                                                                .convert(request));
        authentication.setSecurityContextRepository(new NullSecurityContextRepository());
        authentication.setSuccessHandler(continueAuthenticatedRequest());
        authentication.setFailureHandler(unauthorizedFailureHandler());
        return authentication;
    }

    private AuthenticationEntryPointFailureHandler unauthorizedFailureHandler() {
        AuthenticationEntryPointFailureHandler failures = new AuthenticationEntryPointFailureHandler(UNAUTHORIZED);
        // Preserve the endpoint policy for every AuthenticationException, including provider-service failures.
        failures.setRethrowAuthenticationServiceException(false);
        return failures;
    }

    private AuthenticationSuccessHandler continueAuthenticatedRequest() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response, @NonNull Authentication result) {
                // AuthenticationFilter uses the chain-aware overload; no redirect or session is created.
            }

            @Override
            public void onAuthenticationSuccess(@NonNull HttpServletRequest request,
                    @NonNull HttpServletResponse response, @NonNull FilterChain chain, @NonNull Authentication result)
                    throws IOException, ServletException {
                chain.doFilter(request, response);
            }
        };
    }

    private void configureEndpointChain(HttpSecurity http, Map<String, TrailsEndpointSecurity> endpointsByPath,
            AuthenticationFilter authentication) {
        http.securityMatcher(request -> endpointsByPath.containsKey(endpointPath(request)))
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
            .requestCache(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()))
            .addFilterBefore(authentication, AnonymousAuthenticationFilter.class)
            .exceptionHandling(errors -> errors.authenticationEntryPoint(UNAUTHORIZED))
            .authorizeHttpRequests(authorize -> authorize.anyRequest()
                                                         .authenticated());
    }

    private TrailsEndpointSecurity endpointFor(HttpServletRequest request,
            Map<String, TrailsEndpointSecurity> endpointsByPath) {
        return java.util.Objects.requireNonNull(endpointsByPath.get(endpointPath(request)),
                "Request must match a registered transport security endpoint");
    }

    private String endpointPath(HttpServletRequest request) {
        // Match the container's decoded servlet routing, including encoded spellings of the endpoint.
        return request.getServletPath() + java.util.Optional.ofNullable(request.getPathInfo())
                                                            .orElse("");
    }
}
