package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionAcceptedDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** REST and SSE entry points for starting and observing a test execution. */
@RestController
public class TestExecutionController {

    private final TestExecutionFacadeService executions;
    private final TestExecutionRequestMapper requests;
    private final TestExecutionAcceptedResponseMapper responses;
    private final TestExecutionEventHub eventHub;

    public TestExecutionController(TestExecutionFacadeService executions, TestExecutionRequestMapper requests,
            TestExecutionAcceptedResponseMapper responses, TestExecutionEventHub eventHub) {
        this.executions = Objects.requireNonNull(executions, "Test execution facade is required");
        this.requests = Objects.requireNonNull(requests, "Test execution request mapper is required");
        this.responses = Objects.requireNonNull(responses, "Test execution response mapper is required");
        this.eventHub = Objects.requireNonNull(eventHub, "Test execution event hub is required");
    }

    @PostMapping(value = "/api/test", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("@trailsAuthorization.canExecuteTests(authentication)")
    public ResponseEntity<TestExecutionAcceptedDTO> runTest(@RequestBody TestPlanRunDefinitionDTO request) {
        TestExecutionRequestMapper.MappedRequest mappedRequest = requests.map(request);
        UUID executionId = UUID.randomUUID();
        executions.executeTest(new TestExecutionFacadeService.TestExecution(executionId, mappedRequest.runDefinition(),
                mappedRequest.emailRecipients()));
        return ResponseEntity.accepted()
                             .body(responses.acceptedResponse(executionId));
    }

    @GetMapping(value = "/api/test/events/{executionId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTestExecutionEvents(@PathVariable UUID executionId) {
        return eventHub.subscribe(Objects.requireNonNull(executionId, "Execution identifier is required"));
    }

}
