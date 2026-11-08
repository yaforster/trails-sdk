package io.github.yaforster.trails.adapter.api.rest.run;

import io.github.yaforster.trails.adapter.api.TestExecutionEventHub;
import io.github.yaforster.trails.adapter.api.rest.model.BrowserDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestExecutionAcceptedDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanRunDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunCompletionNotificationDTO;
import io.github.yaforster.trails.app.services.TestExecutionFacadeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TestExecutionControllerTest {

    @Test
    void shouldAcceptMappedRequestAndForwardValidatedDynamicRecipients() {
        TestExecutionFacadeService executions = mock();
        TestExecutionController controller = controller(executions);
        TestPlanRunDefinitionDTO request = new TestPlanRunDefinitionDTO().testPlanID(5L)
                                                                         .browsersToTest(List.of(BrowserDTO.CHROME))
                                                                         .completionNotification(
                                                                                 new TestRunCompletionNotificationDTO().emailEnabled(
                                                                                         true)
                                                                                                                       .recipientEmails(
                                                                                                                               List.of("alice@EXAMPLE.test")));

        ResponseEntity<TestExecutionAcceptedDTO> response = controller.runTest(request);

        ArgumentCaptor<TestExecutionFacadeService.TestExecution> execution = forClass(
                TestExecutionFacadeService.TestExecution.class);
        verify(executions).executeTest(execution.capture());
        assertThat(response.getStatusCode()
                           .value()).isEqualTo(202);
        assertThat(response.getBody()
                           .getLinks()
                           .get("events")
                           .getHref()).contains("/api/test/events/");
        assertThat(execution.getValue()
                            .emailRecipients()).hasValueSatisfying(
                                    recipients -> assertThat(recipients.values()).containsExactly(
                                            "alice@example.test"));
    }

    @Test
    void shouldRejectExplicitNullEmailEnabledOverHttpBeforeSchedulingExecution() throws Exception {
        TestExecutionFacadeService executions = mock();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller(executions))
                                         .setControllerAdvice(new TestExecutionExceptionHandler())
                                         .build();

        mockMvc.perform(MockMvcRequestBuilders.post("/api/test")
                                              .contentType(MediaType.APPLICATION_JSON)
                                              .content("""
                                                      {
                                                        "testPlanID": 5,
                                                        "browsersToTest": ["CHROME"],
                                                        "completionNotification": { "emailEnabled": null }
                                                      }
                                                      """))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("COMPLETION_NOTIFICATION_EMAIL_ENABLED_NULL"));

        verifyNoInteractions(executions);
    }

    @Test
    void shouldRejectInvalidRecipientsWithoutSchedulingExecution() {
        TestExecutionFacadeService executions = mock();
        TestExecutionController controller = controller(executions);
        TestPlanRunDefinitionDTO request = new TestPlanRunDefinitionDTO().testPlanID(5L)
                                                                         .browsersToTest(List.of(BrowserDTO.CHROME))
                                                                         .completionNotification(
                                                                                 new TestRunCompletionNotificationDTO().emailEnabled(
                                                                                         true)
                                                                                                                       .recipientEmails(
                                                                                                                               List.of("not-an-email")));

        assertThatThrownBy(() -> controller.runTest(request)).isInstanceOf(
                TestExecutionRequestValidationException.class);

        verifyNoInteractions(executions);
    }

    private TestExecutionController controller(TestExecutionFacadeService executions) {
        TestExecutionLinkFactory links = new TestExecutionLinkFactory();
        return new TestExecutionController(executions, new TestExecutionRequestMapper(),
                new TestExecutionAcceptedResponseMapper(links), new TestExecutionEventHub(() -> 1L));
    }

}
