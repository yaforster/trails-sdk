package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.TestService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = TrailsSecurityTestApplication.class, properties = {"trails.test.authentication.enabled=true",
        "trails.security.roles.test-manager=manager", "trails.security.roles.tester=tester",
        "trails.security.roles.admin=admin", "trails.security.roles.deployment-reporter=reporter"})
@AutoConfigureMockMvc
class TestExecutionAuthorizationTest {
    @Autowired
    private MockMvc http;
    @MockitoBean
    private TestService executions;
    private static final String REQUEST = "{\"testPlanID\":7,\"browsersToTest\":[\"CHROME\"]}";

    @Test
    void shouldDenyExecution_whenUserOnlyReportsDeployments() throws Exception {
        http.perform(post("/api/test").with(httpBasic("reporter", "password"))
                                      .contentType("application/json")
                                      .content(REQUEST))
            .andExpect(status().isForbidden());
        verifyNoInteractions(executions);
    }

    @ParameterizedTest
    @ValueSource(strings = {"tester", "manager", "admin"})
    void shouldAdmitExecution_whenUserHasExecutionRole(String user) throws Exception {
        http.perform(post("/api/test").with(httpBasic(user, "password"))
                                      .contentType("application/json")
                                      .content(REQUEST))
            .andExpect(status().isAccepted());
        verify(executions).runTest(any());
    }
}
