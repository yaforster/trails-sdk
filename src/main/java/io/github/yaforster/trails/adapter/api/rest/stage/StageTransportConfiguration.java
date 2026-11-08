package io.github.yaforster.trails.adapter.api.rest.stage;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the stage resource exclusively to the validated Trails composition. */
@Configuration(proxyBeanMethods = false)
public class StageTransportConfiguration {

    @Bean
    StageRequestValidator stageRequestValidator(TrailsComposition composition) {
        return new StageRequestValidator(composition.persistence()
                                                    .ports()
                                                    .stageDatabaseService());
    }

    @Bean
    StageResponseAssembler stageResponseAssembler() {
        return new StageResponseAssembler();
    }

    @Bean
    StageController stageController(TrailsComposition composition, TrailsAuthorization authorization,
            StageRequestValidator validator, StageResponseAssembler responses) {
        return new StageController(composition.persistence()
                                              .ports()
                                              .stageDatabaseService(),
                composition.persistence()
                           .ports()
                           .elementDatabaseService(),
                authorization, validator, responses);
    }

}
