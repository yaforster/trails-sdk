package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.PrintFacadeService;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.composition.TrailsPersistenceRegistration;
import io.github.yaforster.trails.sdk.host.print.PrintFacade;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Assembles result documents using exactly one customer-selected printing provider.
 */
@Configuration(proxyBeanMethods = false)
public class PrintTransportConfiguration {

    @Bean
    PrintFacadeService printFacadeService(TrailsComposition composition, HierarchyValidationService hierarchies) {
        TrailsPersistenceRegistration persistence = composition.persistence();
        return new PrintFacade(persistence.ports()
                                          .testSetResultQueryService(),
                persistence.ports()
                           .testPathResultQueryService(),
                persistence.ports()
                           .actionResultQueryService(),
                persistence.ports()
                           .testRunResultDatabaseService(),
                composition.printing()
                           .printService(),
                hierarchies);
    }

    @Bean
    PrintRequestValidator printRequestValidator() {
        return new PrintRequestValidator();
    }

    @Bean
    PrintResponseAssembler printResponseAssembler() {
        return new PrintResponseAssembler();
    }

    @Bean
    PrintController printController(PrintFacadeService prints, PrintRequestValidator validator,
            PrintResponseAssembler responses) {
        return new PrintController(prints, validator, responses);
    }

}
