package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires action-result transport to the selected atomic persistence capability. */
@Configuration(proxyBeanMethods = false)
public class ActionResultTransportConfiguration {

    @Bean
    ActionResultRequestValidator actionResultRequestValidator() {
        return new ActionResultRequestValidator();
    }

    @Bean
    ActionResultResponseAssembler actionResultResponseAssembler() {
        return new ActionResultResponseAssembler();
    }

    @Bean
    ActionResultController actionResultController(TrailsComposition composition, HierarchyValidationService hierarchies,
            ActionResultRequestValidator validator, ActionResultResponseAssembler responses) {
        return new ActionResultController(composition.persistence()
                                                     .ports()
                                                     .actionResultQueryService(),
                hierarchies, validator, responses, composition.persistence()
                                                              .ports()
                                                              .actionResultScreenshotQueryService());
    }

}
