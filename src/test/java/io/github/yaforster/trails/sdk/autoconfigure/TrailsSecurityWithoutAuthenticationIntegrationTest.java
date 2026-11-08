package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.PrintService;
import io.github.yaforster.trails.app.services.ScreenshotDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.ArtifactType;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.TestDataEntry;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import io.github.yaforster.trails.core.persisted.ActionResultType;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.core.persisted.PersistedTestRunHistory;
import io.github.yaforster.trails.core.persisted.PersistedTestRunResult;
import io.github.yaforster.trails.core.persisted.PersistedTestRunStatistics;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;
import io.github.yaforster.trails.core.data.ResultIndicator;
import io.github.yaforster.trails.core.definition.TestPlanDefinition;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.Browser;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.core.test.Position;
import io.github.yaforster.trails.core.test.action.browser.SwitchWebsiteAction;
import io.github.yaforster.trails.core.test.action.value.FixedValueInstruction;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TrailsSecurityTestApplication.class)
@AutoConfigureMockMvc
class TrailsSecurityWithoutAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationDatabaseService applicationDatabaseService;
    @Autowired
    private ActionQueryService actionQueryService;
    @Autowired
    private StageDatabaseService stageDatabaseService;
    @Autowired
    private ElementDatabaseService elementDatabaseService;
    @Autowired
    private ScreenshotDatabaseService screenshotDatabaseService;
    @Autowired
    private TestDataDatabaseService testDataDatabaseService;
    @Autowired
    private TestPlanDatabaseService testPlanDatabaseService;
    @Autowired
    private TestRunResultDatabaseService testRunResultDatabaseService;
    @Autowired
    private TestSetResultQueryService testSetResultQueryService;
    @Autowired
    private TestPathResultQueryService testPathResultQueryService;
    @Autowired
    private ActionResultQueryService actionResultQueryService;
    @Autowired
    private ActionResultScreenshotQueryService actionResultScreenshotQueryService;
    @Autowired
    private ArtifactQueryService artifactQueryService;
    @Autowired
    private PrintService printer;

    @Test
    void shouldPermitTrailsAndNonTrailsRequestsWhenAuthenticationIsNotRegistered() throws Exception {
        mockMvc.perform(get("/api/security-probe"))
               .andExpect(status().isOk());
        mockMvc.perform(get("/user/security-probe"))
               .andExpect(status().isOk());
        mockMvc.perform(post("/api/security-probe"))
               .andExpect(status().isOk());
    }

    @Test
    void shouldNotExposeIdentityBoundUserProfilesWithoutAnAuthenticationAdapter() throws Exception {
        mockMvc.perform(get("/user/profile"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldExposeCompleteCapabilityMetadataWithoutAuthenticationAdapter() throws Exception {
        mockMvc.perform(get("/api/capabilities"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[0].name").value("USER_IMAGE_MAX_HEIGHT"))
               .andExpect(jsonPath("$[0].value").value("1920"))
               .andExpect(jsonPath("$[4].name").value("FILE_DOWNLOAD_TIMEOUT_MILLIS"))
               .andExpect(jsonPath("$[4].value").value("30000"))
               .andExpect(jsonPath("$[5].name").value("USER_FEATURES_ACTIVE"))
               .andExpect(jsonPath("$[5].value").value("false"));
    }

    @Test
    void shouldExposeApplicationResourceWithoutAuthenticationAdapter() throws Exception {
        when(applicationDatabaseService.getApplications(
                new ApplicationDatabaseService.ApplicationPage(0, 20, false))).thenReturn(
                        PagedResult.singlePage(List.of(new PersistedApplication(12L, "Trails"))));
        when(stageDatabaseService.findApplicationIdsWithStages(
                new StageDatabaseService.ApplicationsWithStages(List.of(12L)))).thenReturn(Set.of());

        mockMvc.perform(get("/api/applications"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].id").value(12))
               .andExpect(jsonPath("$._links.create.method").value("PUT"));
    }

    @Test
    void shouldExposeClosedActionCatalogueThroughTheAtomicPersistenceComposition() throws Exception {
        PersistedAction action = new PersistedAction(45L, 34L, 56L, SwitchWebsiteAction.builder()
                                                                                       .actionID(45L)
                                                                                       .label("Open checkout")
                                                                                       .nextActions(List.of())
                                                                                       .position(new Position(
                                                                                               BigDecimal.ONE,
                                                                                               BigDecimal.TEN))
                                                                                       .computationInstruction(
                                                                                               new FixedValueInstruction(
                                                                                                       "https://trails.example/checkout"))
                                                                                       .build());
        when(actionQueryService.listActions(new ActionQueryService.ActionPage(12L, 23L, 34L, 0, 20))).thenReturn(
                java.util.Optional.of(PagedResult.singlePage(List.of(action))));

        mockMvc.perform(get("/api/applications/12/stages/23/testPlans/34/actions"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].actionID").value(45))
               .andExpect(jsonPath("$.items[0].referenceID").value(56))
               .andExpect(jsonPath("$.items[0].details.detailsType").value("SWITCH_WEBSITE"))
               .andExpect(jsonPath("$.items[0].details.valueComputation.computation").value("FIXED"))
               .andExpect(jsonPath("$._links.self.href").value(
                       "/api/applications/12/stages/23/testPlans/34/actions?page=0&size=1"))
               .andExpect(jsonPath("$._links.testPlan.href").value(
                       "/api/applications/12/stages/23/testPlans/34?includeRetired=false"));
    }

    @Test
    void shouldReturnTheContractValidationErrorForAnInvalidActionPage() throws Exception {
        mockMvc.perform(get("/api/applications/12/stages/23/testPlans/34/actions?page=-1&size=0"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("REQUEST_CONSTRAINT_VIOLATION"))
               .andExpect(jsonPath("$[0].path").value("/page"))
               .andExpect(jsonPath("$[1].code").value("REQUEST_CONSTRAINT_VIOLATION"))
               .andExpect(jsonPath("$[1].path").value("/size"));
    }

    @Test
    void shouldExposeTestPlanNavigationThroughTheAtomicPersistenceComposition() throws Exception {
        PersistedTestPlan testPlan = new PersistedTestPlan(34L, 12L, 23L, "Checkout", false);
        when(testPlanDatabaseService.getTestPlans(
                new TestPlanDatabaseService.TestPlanPage(12L, 23L, 0, 20, false))).thenReturn(
                        PagedResult.singlePage(List.of(testPlan)));
        when(testPlanDatabaseService.findTestPlanIdsWithActions(
                new TestPlanDatabaseService.TestPlanActions(12L, 23L, List.of(34L)))).thenReturn(Set.of(34L));

        mockMvc.perform(get("/api/applications/12/stages/23/testPlans"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].id").value(34))
               .andExpect(jsonPath("$.items[0]._links.steps.href").value(
                       "/api/applications/12/stages/23/testPlans/34/actions?page=0&size=20"))
               .andExpect(jsonPath("$.items[0]._links.delete.method").value("DELETE"))
               .andExpect(jsonPath("$._links.create.method").value("PUT"));
    }

    @Test
    void shouldExposeTestRunSummaryAndNavigationThroughTheAtomicPersistenceComposition() throws Exception {
        PersistedTestRunResult result = new PersistedTestRunResult(45L, 12L, 23L, 34L,
                Timestamp.valueOf("2026-10-03 12:34:56"), ResultIndicator.SUCCESS, "Checkout");
        when(testRunResultDatabaseService.listTestRuns(
                new TestRunResultDatabaseService.TestRunPage(12L, 23L, 0, 20))).thenReturn(
                        java.util.Optional.of(PagedResult.singlePage(List.of(result))));

        mockMvc.perform(get("/api/applications/12/stages/23/testruns"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].id").value(45))
               .andExpect(jsonPath("$.items[0].indicator").value("SUCCESS"))
               .andExpect(jsonPath("$.items[0]._links.testSets.href").value(
                       "/api/testruns/12/23/45/browsers?page=0&size=20"))
               .andExpect(jsonPath("$.items[0]._links.print.href").value("/api/testruns/12/23/45/print"))
               .andExpect(
                       jsonPath("$._links.self.href").value("/api/applications/12/stages/23/testruns?page=0&size=1"));
    }

    @Test
    void shouldExposeTestPlanHistoryAndStatisticsLinks() throws Exception {
        PersistedTestRunResult result = new PersistedTestRunResult(45L, 12L, 23L, 34L,
                Timestamp.valueOf("2026-10-03 12:34:56"), ResultIndicator.PARTIAL_SUCCESS, "Checkout");
        PersistedTestRunHistory history = new PersistedTestRunHistory(12L, 23L, 34L, 3L, 1L, 1L, 1L,
                PagedResult.singlePage(List.of(result)));
        when(testRunResultDatabaseService.getTestRunHistory(
                new TestRunResultDatabaseService.TestPlanHistory(12L, 23L, 34L, 0, 20))).thenReturn(
                        java.util.Optional.of(history));
        when(testRunResultDatabaseService.getTestRunStatistics(
                new TestRunResultDatabaseService.TestPlanStatistics(12L, 23L, 34L))).thenReturn(
                        java.util.Optional.of(new PersistedTestRunStatistics(12L, 23L, 34L, 3L, 1L, 1L, 1L)));

        mockMvc.perform(get("/api/applications/12/stages/23/testPlans/34/metrics/history"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].indicator").value("PARTIAL_SUCCESS"))
               .andExpect(jsonPath("$._links.statistics.href").value(
                       "/api/applications/12/stages/23/testPlans/34/statistics"));
        mockMvc.perform(get("/api/applications/12/stages/23/testPlans/34/statistics"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.totalRuns").value(3))
               .andExpect(jsonPath("$._links.history.href").value(
                       "/api/applications/12/stages/23/testPlans/34/metrics/history?page=0&size=50"));
    }

    @Test
    void shouldExposeBrowserResultNavigationThroughTheAtomicPersistenceComposition() throws Exception {
        PersistedTestSetResult result = new PersistedTestSetResult(56L, 45L, 12L, 23L, 250, Browser.CHROME, "Checkout",
                OffsetDateTime.parse("2026-10-03T12:34:56Z"), Browser.CHROME);
        when(testSetResultQueryService.listBrowserResults(
                new TestSetResultQueryService.TestRunBrowserResults(12L, 23L, 45L, 0, 20))).thenReturn(
                        java.util.Optional.of(PagedResult.singlePage(List.of(result))));

        mockMvc.perform(get("/api/testruns/12/23/45/browsers"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].browserToRunIn").value("CHROME"))
               .andExpect(jsonPath("$.items[0].testCaseResult.testedInBrowser").value("CHROME"))
               .andExpect(jsonPath("$.items[0]._links.paths.href").value(
                       "/api/testruns/12/23/45/browsers/56/paths?page=0&size=20"))
               .andExpect(jsonPath("$._links.testRun.href").value("/api/testruns/12/23/45"));
    }

    @Test
    void shouldRejectAPathOutsideItsBrowserResultHierarchy() throws Exception {
        when(testSetResultQueryService.getTestSetResult(56L)).thenReturn(
                java.util.Optional.of(new PersistedTestSetResult(56L, 45L, 12L, 23L, 250, Browser.CHROME, "Checkout",
                        PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.CHROME)));
        when(testPathResultQueryService.getPathResult(78L)).thenReturn(
                java.util.Optional.of(new PersistedTestPathResult(78L, 57L, "Alternative path")));

        mockMvc.perform(get("/api/testruns/12/23/45/browsers/56/paths/78"))
               .andExpect(status().isNotFound());
    }

    @Test
    void shouldExposePathNavigationWhenTheHierarchyIsValid() throws Exception {
        when(testSetResultQueryService.getTestSetResult(56L)).thenReturn(
                java.util.Optional.of(new PersistedTestSetResult(56L, 45L, 12L, 23L, 250, Browser.CHROME, "Checkout",
                        PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.CHROME)));
        when(testPathResultQueryService.listPathResults(
                new TestPathResultQueryService.TestSetPathPage(56L, 0, 20))).thenReturn(
                        java.util.Optional.of(
                                PagedResult.singlePage(List.of(new PersistedTestPathResult(78L, 56L, "Happy path")))));

        mockMvc.perform(get("/api/testruns/12/23/45/browsers/56/paths"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0]._links.actionResults.href").value(
                       "/api/testruns/12/23/45/browsers/56/paths/78/actions/chain"))
               .andExpect(jsonPath("$.items[0]._links.downloadedFiles.href").value(
                       "/api/testruns/12/23/45/browsers/56/paths/78/artifacts?page=0&size=20"));
    }

    @Test
    void shouldExposeActionResultChainAndOptionalScreenshotNavigation() throws Exception {
        stubPathHierarchy();
        PersistedActionResult actionResult = new PersistedActionResult(91L, 78L, 44L, "Submit", "Saved",
                ActionResultType.SUCCESS, null);
        when(actionResultQueryService.listActionResultChain(78L)).thenReturn(
                java.util.Optional.of(List.of(actionResult)));
        when(actionResultScreenshotQueryService.findActionResultIdsInPath(78L)).thenReturn(java.util.Set.of(91L));

        mockMvc.perform(get("/api/testruns/12/23/45/browsers/56/paths/78/actions/chain"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].resultType").value("SUCCESS"))
               .andExpect(jsonPath("$.items[0]._links.screenshot.href").value(
                       "/api/testruns/12/23/45/browsers/56/paths/78/actions/chain/screenshots/91"))
               .andExpect(jsonPath("$._links.pathResult.href").value("/api/testruns/12/23/45/browsers/56/paths/78"));
    }

    @Test
    void shouldServeArtifactsOnlyWithinTheirVerifiedPathHierarchy() throws Exception {
        stubPathHierarchy();
        PersistedArtifact artifact = new PersistedArtifact(92L, 91L, ArtifactType.FILE, "report.csv", "text/csv", 3L,
                "test-key");
        when(artifactQueryService.listArtifacts(new ArtifactQueryService.PathArtifactPage(78L, 0, 20))).thenReturn(
                PagedResult.singlePage(List.of(artifact)));
        when(artifactQueryService.findArtifactInPath(new ArtifactQueryService.PathArtifact(78L, 92L))).thenReturn(
                java.util.Optional.of(new PersistedArtifactFile(92L, "report.csv", "text/csv", new byte[]{1, 2, 3})));

        mockMvc.perform(get("/api/testruns/12/23/45/browsers/56/paths/78/artifacts"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].type").value("OTHER"))
               .andExpect(jsonPath("$.items[0]._links.self.href").value(
                       "/api/testruns/12/23/45/browsers/56/paths/78/artifacts/92"));
        mockMvc.perform(get("/api/testruns/12/23/45/browsers/56/paths/78/artifacts/92"))
               .andExpect(status().isOk())
               .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                                                                                           .contentType("text/csv"))
               .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                                                                                           .bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void shouldStreamThePrintingProviderOutputForAPersistedTestRun() throws Exception {
        PersistedTestRunResult testRun = new PersistedTestRunResult(45L, 12L, 23L, 34L,
                Timestamp.valueOf("2026-10-03 12:34:56"), ResultIndicator.SUCCESS, "Checkout");
        when(testRunResultDatabaseService.getTestRunResult(
                new TestRunResultDatabaseService.TestRunResultDetails(12L, 23L, 45L))).thenReturn(
                        java.util.Optional.of(testRun));
        when(testSetResultQueryService.listBrowserResults(
                new TestSetResultQueryService.TestRunBrowserResults(12L, 23L, 45L, 0, 100))).thenReturn(
                        java.util.Optional.of(PagedResult.singlePage(List.of())));

        byte[] report = "adapter-provided-report".getBytes(StandardCharsets.UTF_8);
        doAnswer(invocation -> {
            PrintService.TestRunPdf request = invocation.getArgument(0);
            assertThat(request.testRunPrintContext()
                              .id()).isEqualTo(45L);
            request.outputStream()
                   .write(report);
            return null;
        }).when(printer)
          .renderTestRunPdf(any());

        byte[] content = mockMvc.perform(get("/api/testruns/12/23/45/print"))
                                .andExpect(status().isOk())
                                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                                                                                                            .contentType(
                                                                                                                    "application/pdf"))
                                .andReturn()
                                .getResponse()
                                .getContentAsByteArray();

        assertThat(content).isEqualTo(report);
    }

    private void stubPathHierarchy() {
        when(testSetResultQueryService.getTestSetResult(56L)).thenReturn(
                java.util.Optional.of(new PersistedTestSetResult(56L, 45L, 12L, 23L, 250, Browser.CHROME, "Checkout",
                        PersistedTestSetResult.NO_TEST_CASE_TIMESTAMP, Browser.CHROME)));
        when(testPathResultQueryService.getPathResult(78L)).thenReturn(
                java.util.Optional.of(new PersistedTestPathResult(78L, 56L, "Happy path")));
    }

    @Test
    void shouldReturnTheContractValidationErrorForAnInvalidTestPlanDefinition() throws Exception {
        mockMvc.perform(put("/api/applications/12/stages/23/testPlans").contentType("application/json")
                                                                       .content("{\"label\":\" \",\"testSteps\":[]}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("TEST_PLAN_LABEL_EMPTY"))
               .andExpect(jsonPath("$[1].code").value("TEST_PLAN_TEST_STEPS_EMPTY"));
    }

    @Test
    void shouldCreateValidatedTestPlanWithTheClosedActionCatalogue() throws Exception {
        when(testPlanDatabaseService.storeTestPlan(any(TestPlanDefinition.class))).thenReturn(
                new PersistedTestPlan(34L, 12L, 23L, "Checkout", false));
        when(testPlanDatabaseService.hasActions(34L)).thenReturn(true);

        mockMvc.perform(put("/api/applications/12/stages/23/testPlans").contentType("application/json")
                                                                       .content(
                                                                               """
                                                                                       { "label": "Checkout", "testSteps": [
                                                                                         { "referenceID": 1, "nextActions": [], "position": { "xCoordinate": 10, "yCoordinate": 20 },
                                                                                           "details": { "detailsType": "SWITCH_WEBSITE", "valueComputation": { "computation": "FIXED", "value": "https://trails.example/checkout" } }
                                                                                         }
                                                                                       ] }
                                                                                       """))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(34))
               .andExpect(jsonPath("$._links.steps.method").value("GET"))
               .andExpect(jsonPath("$._links.execute.method").value("POST"));

        ArgumentCaptor<TestPlanDefinition> definition = ArgumentCaptor.forClass(TestPlanDefinition.class);
        verify(testPlanDatabaseService).storeTestPlan(definition.capture());
        assertThat(definition.getValue()
                             .applicationID()).isEqualTo(12L);
        assertThat(definition.getValue()
                             .stageID()).isEqualTo(23L);
        assertThat(definition.getValue()
                             .label()).isEqualTo("Checkout");
        assertThat(definition.getValue()
                             .actions()).hasSize(1);
        Action action = definition.getValue()
                                  .actions()
                                  .getFirst();
        assertThat(action).isInstanceOf(SwitchWebsiteAction.class);
        assertThat(action.getActionID()).isEqualTo(1L);
        assertThat(action.getNextActions()).isEmpty();
        assertThat(action.getPosition()).isEqualTo(new Position(BigDecimal.TEN, BigDecimal.valueOf(20)));
        assertThat(((SwitchWebsiteAction) action).getComputationInstruction()).isEqualTo(
                new FixedValueInstruction("https://trails.example/checkout"));
    }

    @Test
    void shouldReturnTheContractValidationErrorForAnInvalidApplicationRequest() throws Exception {
        mockMvc.perform(put("/api/applications").contentType("application/json")
                                                .content("{\"label\":\" \"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("APPLICATION_LABEL_EMPTY"))
               .andExpect(jsonPath("$[0].path").value("/label"));
    }

    @Test
    void shouldExposeStageNavigationWithoutAuthenticationAdapter() throws Exception {
        when(stageDatabaseService.getStages(new StageDatabaseService.StagePage(12L, 0, 20, false))).thenReturn(
                PagedResult.singlePage(List.of(new PersistedStage(23L, 12L, "Production", "https://trails.example"))));
        when(elementDatabaseService.findStageIdsWithElements(
                new ElementDatabaseService.CandidateStages(12L, List.of(23L)))).thenReturn(Set.of(23L));

        mockMvc.perform(get("/api/applications/12/stages"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].id").value(23))
               .andExpect(jsonPath("$.items[0]._links.elements.method").value("GET"))
               .andExpect(jsonPath("$._links.create.method").value("PUT"));
    }

    @Test
    void shouldReturnTheContractValidationErrorForAnInvalidStageRequest() throws Exception {
        mockMvc.perform(put("/api/applications/12/stages").contentType("application/json")
                                                          .content("{\"label\":\" \"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("STAGE_LABEL_EMPTY"))
               .andExpect(jsonPath("$[1].code").value("STAGE_URL_EMPTY"));
    }

    @Test
    void shouldExposeElementNavigationAndManagementLinksWithoutAuthenticationAdapter() throws Exception {
        PersistedElement element = new PersistedElement(34L, "Login", LocatorType.CSS, 12L, 23L, "#login",
                ElementType.BUTTON);
        when(elementDatabaseService.getElements(
                new ElementDatabaseService.ElementPage(12L, 23L, 0, 20, false))).thenReturn(
                        PagedResult.singlePage(List.of(element)));
        when(screenshotDatabaseService.findAllIdsByElementIds(
                new ScreenshotDatabaseService.ElementScreenshotIds(List.of(34L)))).thenReturn(Map.of(34L, 56L));

        mockMvc.perform(get("/api/applications/12/stages/23/elements"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].type").value("BUTTON"))
               .andExpect(jsonPath("$.items[0]._links.screenshot.method").value("GET"))
               .andExpect(jsonPath("$.items[0]._links.promote.templated").value(true))
               .andExpect(jsonPath("$._links.create.method").value("PUT"));
    }

    @Test
    void shouldReturnTheContractValidationErrorForAnInvalidElementRequest() throws Exception {
        mockMvc.perform(put("/api/applications/12/stages/23/elements").contentType("application/json")
                                                                      .content("{\"label\":\" \"}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("ELEMENT_TYPE_NULL"))
               .andExpect(jsonPath("$[1].code").value("ELEMENT_LABEL_EMPTY"))
               .andExpect(jsonPath("$[2].code").value("ELEMENT_LOCATOR_STRING_EMPTY"))
               .andExpect(jsonPath("$[3].code").value("ELEMENT_LOCATOR_TYPE_NULL"));
    }

    @Test
    void shouldRejectUnsupportedElementScreenshotContentBeforePersistence() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain",
                "not an image".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/applications/12/stages/23/elements/34/screenshot").file(file))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("ELEMENT_SCREENSHOT_FILE_TYPE_UNSUPPORTED"));
    }

    @Test
    void shouldExposeTestDataNavigationWithoutAuthenticationAdapter() throws Exception {
        when(testDataDatabaseService.listTestData(new TestDataDatabaseService.TestDataPage(0, 20, false))).thenReturn(
                PagedResult.singlePage(
                        List.of(new TestDataSet(45L, "Credentials", Instant.parse("2026-10-03T12:00:00Z"),
                                List.of(new TestDataEntry("username", "alice")), false))));

        mockMvc.perform(get("/api/test-data"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].id").value(45))
               .andExpect(jsonPath("$.items[0]._links.delete.method").value("DELETE"))
               .andExpect(jsonPath("$._links.create.method").value("PUT"))
               .andExpect(jsonPath("$._links.create.href").value("/api/test-data"));
    }

    @Test
    void shouldReturnTheContractValidationErrorForAnInvalidTestDataRequest() throws Exception {
        mockMvc.perform(put("/api/test-data").contentType("application/json")
                                             .content("{\"label\":\" \",\"values\":[null]}"))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("TEST_DATA_LABEL_EMPTY"))
               .andExpect(jsonPath("$[1].code").value("TEST_DATA_ENTRY_NULL"));
    }

}
