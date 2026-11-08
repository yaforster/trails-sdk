package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.*;
import io.github.yaforster.trails.core.CapabilityLabel;
import io.github.yaforster.trails.core.test.ActionGraphLimits;
import io.github.yaforster.trails.sdk.composition.TrailsComposition;
import io.github.yaforster.trails.sdk.host.capability.SdkCapabilitiesService;
import io.github.yaforster.trails.sdk.host.execution.TestRunExecutor;
import io.github.yaforster.trails.sdk.host.execution.TestStepArranger;
import io.github.yaforster.trails.sdk.host.testdata.TestDataValueResolver;
import io.github.yaforster.trails.sdk.host.time.SystemTimeSource;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Creates SDK-owned execution support from the already validated adapter composition.
 */
class TrailsHostConfiguration {

    @Bean
    TimeSource trailsTimeSource() {
        return new SystemTimeSource();
    }

    @Bean
    CapabilitiesService capabilitiesService(TrailsComposition composition, List<CapabilityContributor> contributors) {
        return new SdkCapabilitiesService(contributors);
    }

    @Bean
    CapabilityContributor userFeaturesCapabilityContributor(TrailsComposition composition) {
        String active = String.valueOf(composition.authentication()
                                                  .isPresent());
        return () -> List.of(new CapabilityContribution(CapabilityLabel.USER_FEATURES_ACTIVE, active),
                new CapabilityContribution(CapabilityLabel.AUTHENTICATION_REQUIRED,
                        String.valueOf(composition.authenticationRequired())));
    }

    @Bean
    TestStepArrangementService testStepArrangementService(ActionGraphLimits limits) {
        return new TestStepArranger(limits);
    }

    @Bean
    ActionGraphLimits actionGraphLimits(ActionGraphProperties properties) {
        return properties.limits();
    }

    @Bean
    CapabilityContributor actionGraphCapabilityContributor(ActionGraphLimits limits) {
        return () -> List.of(
                new CapabilityContribution(CapabilityLabel.TEST_PLAN_MAXIMUM_ACTIONS,
                        Integer.toString(limits.maximumActions())),
                new CapabilityContribution(CapabilityLabel.TEST_PLAN_MAXIMUM_PATHS,
                        Integer.toString(limits.maximumPaths())),
                new CapabilityContribution(CapabilityLabel.TEST_PLAN_MAXIMUM_ACTION_OCCURRENCES,
                        Integer.toString(limits.maximumActionOccurrences())));
    }

    @Bean
    TestDataValueResolutionService testDataValueResolutionService(TrailsComposition composition) {
        return new TestDataValueResolver(composition.persistence()
                                                    .ports()
                                                    .testDataDatabaseService());
    }

    @Bean(destroyMethod = "close")
    TestService testService(TrailsComposition composition, TestStepArrangementService testSteps, TimeSource time,
            TestExecutionProperties properties) {
        return new TestRunExecutor(composition.browser()
                                              .webdriverConfigurationFactory(),
                composition.persistence()
                           .ports()
                           .testPlanDatabaseService(),
                testSteps, time, composition.persistence()
                                            .ports()
                                            .testRunResultDatabaseService(),
                properties.limits());
    }

}
