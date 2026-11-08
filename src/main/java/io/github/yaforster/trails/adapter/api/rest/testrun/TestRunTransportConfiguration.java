package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires test-run read transport exclusively to the validated persistence composition. */
@Configuration(proxyBeanMethods = false)
public class TestRunTransportConfiguration {

    @Bean
    TestRunRequestValidator testRunRequestValidator() {
        return new TestRunRequestValidator();
    }

    @Bean
    TestRunResponseAssembler testRunResponseAssembler() {
        return new TestRunResponseAssembler();
    }

    @Bean
    TestRunController testRunController(TrailsComposition composition, TestRunRequestValidator validator,
            TestRunResponseAssembler responses) {
        return new TestRunController(composition.persistence()
                                                .ports()
                                                .testRunResultDatabaseService(),
                validator, responses);
    }

}
