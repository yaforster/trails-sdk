package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.sdk.security.TrailsEndpointSecurity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.assertj.core.api.Assertions.assertThat;

class TrailsEndpointSecurityConfigurationTest {
    @Test
    void shouldRejectDuplicateEndpoints_whenTwoModulesClaimSamePath() {
        new WebApplicationContextRunner().withUserConfiguration(TrailsSecurityTestApplication.class,
                DuplicateEndpoints.class)
                                         .run(context -> assertThat(context.getStartupFailure()).hasRootCauseMessage(
                                                 "Duplicate transport security endpoint: /mcp"));
    }

    @Configuration(proxyBeanMethods = false)
    static class DuplicateEndpoints {
        @Bean
        TrailsEndpointSecurity first() {
            return new TrailsEndpointSecurity("/mcp", authentication -> authentication, request -> null);
        }

        @Bean
        TrailsEndpointSecurity second() {
            return first();
        }
    }
}
