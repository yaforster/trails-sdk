package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the deployment resource exclusively to the validated Trails composition. */
@Configuration(proxyBeanMethods = false)
public class DeploymentTransportConfiguration {

    @Bean
    DeploymentRequestValidator deploymentRequestValidator() {
        return new DeploymentRequestValidator();
    }

    @Bean
    DeploymentResponseAssembler deploymentResponseAssembler() {
        return new DeploymentResponseAssembler();
    }

    @Bean
    DeploymentController deploymentController(TrailsComposition composition, TrailsAuthorization authorization,
            DeploymentRequestValidator validator, DeploymentResponseAssembler responses) {
        return new DeploymentController(composition.persistence()
                                                   .ports()
                                                   .deploymentDatabaseService(),
                authorization, validator, responses);
    }

}
