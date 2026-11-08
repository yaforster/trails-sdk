package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.ResourceExceptionHandler;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the application resource exclusively to the validated Trails composition. */
@Configuration(proxyBeanMethods = false)
public class ApplicationTransportConfiguration {

    @Bean
    ApplicationRequestValidator applicationRequestValidator(TrailsComposition composition) {
        return new ApplicationRequestValidator(composition.persistence()
                                                          .ports()
                                                          .applicationDatabaseService());
    }

    @Bean
    ApplicationResponseAssembler applicationResponseAssembler() {
        return new ApplicationResponseAssembler();
    }

    @Bean
    ApplicationController applicationController(TrailsComposition composition, TrailsAuthorization authorization,
            ApplicationRequestValidator validator, ApplicationResponseAssembler responses) {
        return new ApplicationController(composition.persistence()
                                                    .ports()
                                                    .applicationDatabaseService(),
                composition.persistence()
                           .ports()
                           .stageDatabaseService(),
                authorization, validator, responses);
    }

    @Bean
    ResourceExceptionHandler resourceExceptionHandler() {
        return new ResourceExceptionHandler();
    }

}
