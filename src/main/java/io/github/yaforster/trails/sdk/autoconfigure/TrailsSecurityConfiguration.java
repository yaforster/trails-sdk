package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsAuthenticationRegistration;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import io.github.yaforster.trails.sdk.security.TrailsIdentityService;
import io.github.yaforster.trails.sdk.security.TrailsSecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Creates the SDK-owned HTTP security boundary around an optional authentication mechanism.
 */
@EnableMethodSecurity
@EnableConfigurationProperties(TrailsSecurityProperties.class)
class TrailsSecurityConfiguration {

    @Bean
    TrailsAuthorization trailsAuthorization(TrailsComposition composition, TrailsSecurityProperties properties) {
        return new TrailsAuthorization(composition, properties);
    }

    @Bean
    TrailsIdentityService trailsIdentityService(TrailsComposition composition) {
        return new TrailsIdentityService(composition);
    }

    @Bean
    @ConditionalOnMissingBean(TrailsAuthenticationRegistration.class)
    UserDetailsService trailsAnonymousUserDetailsService() {
        return username -> {
            throw new UsernameNotFoundException("Trails has no authentication adapter");
        };
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    SecurityFilterChain trailsSecurityFilterChain(HttpSecurity httpSecurity, TrailsComposition composition,
            java.util.Map<String, SecurityFilterChain> customerSecurityFilterChains) throws Exception {
        rejectCustomerSecurityFilterChains(customerSecurityFilterChains);
        if (composition.authentication()
                       .isEmpty()) {
            httpSecurity.cors(Customizer.withDefaults())
                        .csrf(AbstractHttpConfigurer::disable)
                        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
            httpSecurity.authorizeHttpRequests(authorize -> authorize.anyRequest()
                                                                     .permitAll());
            return httpSecurity.build();
        }
        httpSecurity.securityMatcher("/api/**", "/user/**")
                    .cors(Customizer.withDefaults());
        composition.authentication()
                   .orElseThrow()
                   .mechanism()
                   .configure(httpSecurity);
        httpSecurity.authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.OPTIONS, "/**")
                                                                 .permitAll()
                                                                 .requestMatchers("/api/capabilities")
                                                                 .permitAll()
                                                                 .anyRequest()
                                                                 .authenticated());
        return httpSecurity.build();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    WebMvcConfigurer trailsCorsConfiguration() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOriginPatterns("*")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(false);
            }
        };
    }

    private void rejectCustomerSecurityFilterChains(
            java.util.Map<String, SecurityFilterChain> customerSecurityFilterChains) {
        if (customerSecurityFilterChains.keySet()
                                        .stream()
                                        .anyMatch(name -> !name.equals(
                                                TrailsEndpointSecurityConfiguration.ENDPOINT_SECURITY_FILTER_CHAIN_BEAN_NAME))) {
            throw new IllegalStateException(
                    "Customer SecurityFilterChain beans are not supported; register one TrailsAuthenticationRegistration for user authentication or TrailsEndpointSecurity for isolated transport credentials instead");
        }
    }

}
