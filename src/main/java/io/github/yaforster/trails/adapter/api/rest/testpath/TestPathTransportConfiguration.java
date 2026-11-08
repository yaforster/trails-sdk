package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.validation.HierarchyValidationServiceImpl;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires hierarchy-verified test-path transport to the complete persistence composition.
 */
@Configuration(proxyBeanMethods = false)
public class TestPathTransportConfiguration {

    @Bean
    HierarchyValidationService hierarchyValidationService(TrailsComposition composition) {
        return new HierarchyValidationServiceImpl(composition.persistence()
                                                             .ports()
                                                             .testSetResultQueryService(),
                composition.persistence()
                           .ports()
                           .testPathResultQueryService());
    }

    @Bean
    TestPathRequestValidator testPathRequestValidator() {
        return new TestPathRequestValidator();
    }

    @Bean
    TestPathResponseAssembler testPathResponseAssembler() {
        return new TestPathResponseAssembler();
    }

    @Bean
    TestPathController testPathController(TrailsComposition composition, HierarchyValidationService hierarchies,
            TestPathRequestValidator validator, TestPathResponseAssembler responses) {
        return new TestPathController(composition.persistence()
                                                 .ports()
                                                 .testPathResultQueryService(),
                hierarchies, validator, responses);
    }

}
