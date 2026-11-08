package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.asyncapi.run.TestExecutionEventFacade;
import io.github.yaforster.trails.adapter.api.asyncapi.run.TestExecutionEventPublisherImpl;
import io.github.yaforster.trails.adapter.api.rest.action.ActionTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.actionresult.ActionResultTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.application.ApplicationTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.artifact.ArtifactTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.capabilities.CapabilitiesTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.deployment.DeploymentTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.element.ElementTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.print.PrintTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.stage.StageTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.testdata.TestDataTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.testplan.TestPlanTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.testpath.TestPathTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.testset.TestSetTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.testrun.TestRunTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.user.UserProfileTransportConfiguration;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionAcceptedResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionController;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionExceptionHandler;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionLinkFactory;
import io.github.yaforster.trails.adapter.api.rest.run.TestExecutionRequestMapper;
import io.github.yaforster.trails.app.TestExecutionEventPublisher;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import io.github.yaforster.trails.app.services.TestService;
import io.github.yaforster.trails.app.services.TimeSource;
import io.github.yaforster.trails.sdk.host.execution.TestExecutionCoordinator;
import io.github.yaforster.trails.sdk.notification.NotifierDispatcher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/** Registers the SDK-owned REST and SSE execution transport. */

@Import({CapabilitiesTransportConfiguration.class, ApplicationTransportConfiguration.class,
        ActionTransportConfiguration.class, ActionResultTransportConfiguration.class,
        ArtifactTransportConfiguration.class, StageTransportConfiguration.class, ElementTransportConfiguration.class,
        TestDataTransportConfiguration.class, DeploymentTransportConfiguration.class,
        TestPlanTransportConfiguration.class, TestRunTransportConfiguration.class, TestSetTransportConfiguration.class,
        TestPathTransportConfiguration.class, PrintTransportConfiguration.class,
        UserProfileTransportConfiguration.class})
class TrailsTransportConfiguration {

    @Bean
    TestExecutionEventHub testExecutionEventHub(TimeSource timeSource) {
        return new TestExecutionEventHub(timeSource);
    }

    @Bean
    TestExecutionLinkFactory testExecutionLinkFactory() {
        return new TestExecutionLinkFactory();
    }

    @Bean
    TestExecutionEventFacade testExecutionEventFacade(TestExecutionLinkFactory links) {
        return new TestExecutionEventFacade(links);
    }

    @Bean
    TestExecutionEventPublisher testExecutionEventPublisher(TestExecutionEventFacade events,
            TestExecutionEventHub eventHub) {
        return new TestExecutionEventPublisherImpl(events, eventHub);
    }

    @Bean
    TestExecutionFacadeService testExecutionFacadeService(TestService testService, TestExecutionEventPublisher events,
            NotifierDispatcher notifiers) {
        return new TestExecutionCoordinator(testService, events, notifiers);
    }

    @Bean
    TestExecutionRequestMapper testExecutionRequestMapper() {
        return new TestExecutionRequestMapper();
    }

    @Bean
    TestExecutionAcceptedResponseMapper testExecutionAcceptedResponseMapper(TestExecutionLinkFactory links) {
        return new TestExecutionAcceptedResponseMapper(links);
    }

    @Bean
    TestExecutionController testExecutionController(TestExecutionFacadeService executions,
            TestExecutionRequestMapper requests, TestExecutionAcceptedResponseMapper responses,
            TestExecutionEventHub eventHub) {
        return new TestExecutionController(executions, requests, responses, eventHub);
    }

    @Bean
    TestExecutionExceptionHandler testExecutionExceptionHandler() {
        return new TestExecutionExceptionHandler();
    }

}
