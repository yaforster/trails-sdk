package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires artifact metadata and binary transport to the selected persistence capability.
 */
@Configuration(proxyBeanMethods = false)
public class ArtifactTransportConfiguration {

    @Bean
    ArtifactRequestValidator artifactRequestValidator() {
        return new ArtifactRequestValidator();
    }

    @Bean
    ArtifactResponseAssembler artifactResponseAssembler() {
        return new ArtifactResponseAssembler();
    }

    @Bean
    ArtifactController artifactController(TrailsComposition composition, HierarchyValidationService hierarchies,
            ArtifactRequestValidator validator, ArtifactResponseAssembler responses) {
        return new ArtifactController(composition.persistence()
                                                 .ports()
                                                 .artifactQueryService(),
                composition.persistence()
                           .ports()
                           .actionResultScreenshotQueryService(),
                hierarchies, validator, responses);
    }

}
