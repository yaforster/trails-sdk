package io.github.yaforster.trails.sdk.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(classes = TrailsSecurityTestApplication.class, properties = {
        "trails.execution.graph.maximum-actions=17", "trails.execution.graph.maximum-paths=13",
        "trails.execution.graph.maximum-action-occurrences=71"})
@AutoConfigureMockMvc
class ActionGraphMetadataTest {
    @Autowired
    private MockMvc http;

    @Test
    void shouldPublishConfiguredLimits_whenClientLoadsCapabilities() throws Exception {
        http.perform(get("/api/capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.name == 'TEST_PLAN_MAXIMUM_ACTIONS')].value").value(
                    org.hamcrest.Matchers.contains("17")))
            .andExpect(jsonPath("$[?(@.name == 'TEST_PLAN_MAXIMUM_PATHS')].value").value(
                    org.hamcrest.Matchers.contains("13")))
            .andExpect(jsonPath("$[?(@.name == 'TEST_PLAN_MAXIMUM_ACTION_OCCURRENCES')].value").value(
                    org.hamcrest.Matchers.contains("71")));
    }

    @Test
    void shouldRejectNonpositiveLimits_whenConfiguringGraphPreparation() {
        assertThatThrownBy(() -> new ActionGraphProperties(0, 1, 1)).isInstanceOf(IllegalArgumentException.class);
    }
}
