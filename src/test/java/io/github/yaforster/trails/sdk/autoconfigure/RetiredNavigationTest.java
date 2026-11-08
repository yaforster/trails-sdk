package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.*;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = TrailsSecurityTestApplication.class)
@AutoConfigureMockMvc
class RetiredNavigationTest {
    @Autowired
    private MockMvc http;
    @MockitoBean
    private ApplicationDatabaseService applications;
    @MockitoBean
    private StageDatabaseService stages;
    @MockitoBean
    private ElementDatabaseService elements;
    @MockitoBean
    private TestPlanDatabaseService testPlans;

    @Test
    void shouldRetainRetirementScope_whenNavigatingApplicationPages() throws Exception {
        when(applications.getApplications(new ApplicationDatabaseService.ApplicationPage(0, 1, true))).thenReturn(
                new PagedResult<>(List.of(new PersistedApplication(1L, "Old", true)), 0, 1, 2));
        http.perform(get("/api/applications?page=0&size=1&includeRetired=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.next.href").value("/api/applications?page=1&size=1&includeRetired=true"))
            .andExpect(jsonPath("$.items[0]._links.self.href").value("/api/applications/1?includeRetired=true"));
    }

    @Test
    void shouldRetainRetirementScope_whenNavigatingStagePages() throws Exception {
        when(stages.getStages(new StageDatabaseService.StagePage(1L, 0, 1, true))).thenReturn(
                new PagedResult<>(List.of(new PersistedStage(2L, 1L, "Old", "https://example.test", true)), 0, 1, 2));
        http.perform(get("/api/applications/1/stages?page=0&size=1&includeRetired=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.next.href").value(
                    "/api/applications/1/stages?page=1&size=1&includeRetired=true"))
            .andExpect(
                    jsonPath("$.items[0]._links.self.href").value("/api/applications/1/stages/2?includeRetired=true"))
            .andExpect(jsonPath("$.items[0]._links.testPlans.href").value(
                    "/api/applications/1/stages/2/testPlans?page=0&size=20&includeRetired=true"));
    }

    @Test
    void shouldRetainRetirementScope_whenNavigatingElementPages() throws Exception {
        PersistedElement retiredElement = new PersistedElement(3L, "old",
                io.github.yaforster.trails.core.test.action.element.LocatorType.CSS, 1L, 2L, "#old",
                io.github.yaforster.trails.core.data.ElementType.BUTTON, true);
        when(elements.getElements(new ElementDatabaseService.ElementPage(1L, 2L, 0, 1, true))).thenReturn(
                new PagedResult<>(List.of(retiredElement), 0, 1, 2));

        http.perform(get("/api/applications/1/stages/2/elements?page=0&size=1&includeRetired=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.next.href").value(
                    "/api/applications/1/stages/2/elements?page=1&size=1&includeRetired=true"))
            .andExpect(jsonPath("$.items[0]._links.self.href").value(
                    "/api/applications/1/stages/2/elements/3?includeRetired=true"));
    }

    @Test
    void shouldRetainRetirementScope_whenNavigatingTestPlanPages() throws Exception {
        PersistedTestPlan retiredPlan = new PersistedTestPlan(4L, 1L, 2L, "old", true, List.of());
        when(testPlans.getTestPlans(new TestPlanDatabaseService.TestPlanPage(1L, 2L, 0, 1, true))).thenReturn(
                new PagedResult<>(List.of(retiredPlan), 0, 1, 2));

        http.perform(get("/api/applications/1/stages/2/testPlans?page=0&size=1&includeRetired=true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$._links.next.href").value(
                    "/api/applications/1/stages/2/testPlans?page=1&size=1&includeRetired=true"))
            .andExpect(jsonPath("$.items[0]._links.self.href").value(
                    "/api/applications/1/stages/2/testPlans/4?includeRetired=true"));
    }
}
