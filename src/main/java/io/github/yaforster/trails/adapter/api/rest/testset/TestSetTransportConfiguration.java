package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires browser-result transport to the complete validated persistence capability. */
@Configuration(proxyBeanMethods = false)
public class TestSetTransportConfiguration {

    @Bean
    TestSetRequestValidator testSetRequestValidator() {
        return new TestSetRequestValidator();
    }

    @Bean
    TestSetResponseAssembler testSetResponseAssembler() {
        return new TestSetResponseAssembler();
    }

    @Bean
    TestSetController testSetController(TrailsComposition composition, TestSetRequestValidator validator,
            TestSetResponseAssembler responses) {
        return new TestSetController(composition.persistence()
                                                .ports()
                                                .testSetResultQueryService(),
                validator, responses);
    }

}
