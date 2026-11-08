package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the test-data resource exclusively to the validated Trails composition. */
@Configuration(proxyBeanMethods = false)
public class TestDataTransportConfiguration {

    @Bean
    TestDataRequestValidator testDataRequestValidator() {
        return new TestDataRequestValidator();
    }

    @Bean
    TestDataResponseAssembler testDataResponseAssembler() {
        return new TestDataResponseAssembler();
    }

    @Bean
    TestDataController testDataController(TrailsComposition composition, TrailsAuthorization authorization,
            TestDataRequestValidator validator, TestDataResponseAssembler responses) {
        return new TestDataController(composition.persistence()
                                                 .ports()
                                                 .testDataDatabaseService(),
                authorization, validator, responses);
    }

}
