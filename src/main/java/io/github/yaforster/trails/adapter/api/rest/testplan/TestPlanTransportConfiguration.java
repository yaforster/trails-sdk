package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.ActionElementReferenceResolver;
import io.github.yaforster.trails.core.test.ActionGraphLimits;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Wires the test-plan resource exclusively to the validated Trails composition. */
@Configuration(proxyBeanMethods = false)
@Import(ActionDetailsRawJacksonCustomizer.class)
public class TestPlanTransportConfiguration {

    @Bean
    TestPlanActionGraphRequestValidator testPlanActionGraphRequestValidator(ActionGraphLimits limits) {
        return new TestPlanActionGraphRequestValidator(limits);
    }

    @Bean
    TestPlanRequestValidator testPlanRequestValidator(TrailsComposition composition,
            TestPlanActionGraphRequestValidator actionGraphs) {
        return new TestPlanRequestValidator(composition.persistence()
                                                       .ports()
                                                       .testPlanDatabaseService(),
                actionGraphs);
    }

    @Bean
    TestPlanRequestMapper testPlanRequestMapper(ActionMapper actionMapper) {
        return new TestPlanRequestMapper(actionMapper);
    }

    @Bean
    TestPlanResponseAssembler testPlanResponseAssembler() {
        return new TestPlanResponseAssembler();
    }

    @Bean
    TestPlanController testPlanController(TrailsComposition composition, TrailsAuthorization authorization,
            TestPlanRequestValidator validator, TestPlanRequestMapper requestMapper,
            TestPlanResponseAssembler responses, ActionElementReferenceResolver elementReferences) {
        return new TestPlanController(composition.persistence()
                                                 .ports()
                                                 .testPlanDatabaseService(),
                composition.persistence()
                           .ports()
                           .stageDatabaseService(),
                elementReferences, authorization, validator, requestMapper, responses);
    }

}
