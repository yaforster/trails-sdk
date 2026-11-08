package io.github.yaforster.trails.sdk.autoconfigure;

import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.app.services.DeploymentDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.ElementDatabaseService;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.app.services.UserProfileService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.data.ElementType;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.definition.ElementDefinition;
import io.github.yaforster.trails.core.definition.StageDefinition;
import io.github.yaforster.trails.core.definition.TestDataSetDefinition;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import io.github.yaforster.trails.core.persisted.PersistedElement;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import io.github.yaforster.trails.core.persisted.PersistedStage;
import io.github.yaforster.trails.core.test.action.element.LocatorType;
import io.github.yaforster.trails.core.user.UserProfile;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TrailsSecurityTestApplication.class, properties = {"trails.test.authentication.enabled=true",
        "trails.security.roles.test-manager=manager", "trails.security.roles.tester=tester",
        "trails.security.roles.admin=admin", "trails.security.roles.deployment-reporter=reporter"})
@AutoConfigureMockMvc
class TrailsSecurityWithAuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ApplicationDatabaseService applicationDatabaseService;
    @Autowired
    private StageDatabaseService stageDatabaseService;
    @Autowired
    private ElementDatabaseService elementDatabaseService;
    @Autowired
    private TestDataDatabaseService testDataDatabaseService;
    @Autowired
    private DeploymentDatabaseService deploymentDatabaseService;
    @Autowired
    private UserProfileService userProfileService;

    @Test
    void shouldRequireAuthenticationForTrailsApiAndUserRoutes() throws Exception {
        mockMvc.perform(get("/api/security-probe"))
               .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/user/security-probe"))
               .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/capabilities"))
               .andExpect(status().isOk());
        mockMvc.perform(get("/security-probe"))
               .andExpect(status().isOk());
    }

    @Test
    void shouldApplyTrailsRolesAfterTheAdapterAuthenticatesTheRequest() throws Exception {
        mockMvc.perform(get("/api/security-authorized-probe").with(httpBasic("tester", "password")))
               .andExpect(status().isOk());
    }

    @Test
    void shouldExposeUserProfilesAndCapabilityWhenAnAuthenticationAdapterProvidesAnIdentity() throws Exception {
        UUID testerId = UUID.nameUUIDFromBytes("tester".getBytes(StandardCharsets.UTF_8));
        when(userProfileService.getOrCreateProfile(testerId)).thenReturn(
                new UserProfile(testerId, "Ada", "Lovelace", "ada@example.test", null, false));

        mockMvc.perform(get("/api/capabilities"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$[5].name").value("USER_FEATURES_ACTIVE"))
               .andExpect(jsonPath("$[5].value").value("true"));
        mockMvc.perform(get("/user/profile").with(httpBasic("tester", "password")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.userId").value(testerId.toString()))
               .andExpect(jsonPath("$.links.self.href").value("/user/profile"))
               .andExpect(jsonPath("$.links.update.method").value("PUT"));
    }

    @Test
    void shouldRestrictStoredUserProfilesToAdministrators() throws Exception {
        UUID userId = UUID.fromString("d82074f7-a02e-447e-8388-76225b5c9dc8");
        when(userProfileService.listUsers(new UserProfileService.UserPage(0, 20))).thenReturn(
                PagedResult.singlePage(List.of(new UserProfile(userId, null, null, null, null, false))));

        mockMvc.perform(get("/user/profiles").with(httpBasic("tester", "password")))
               .andExpect(status().isForbidden());
        mockMvc.perform(get("/user/profiles").with(httpBasic("admin", "password")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.items[0].userId").value(userId.toString()))
               .andExpect(jsonPath("$.items[0].links.deleteUserData.href").value("/user/profile/" + userId));
    }

    @Test
    void shouldReturnAContractValidationErrorForAnInvalidProfileUpdate() throws Exception {
        mockMvc.perform(put("/user/profile").contentType("application/json")
                                            .content("{\"email\":\"invalid\"}")
                                            .with(httpBasic("tester", "password")))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$[0].code").value("REQUEST_CONSTRAINT_VIOLATION"))
               .andExpect(jsonPath("$[0].path").value("/email"));
    }

    @Test
    void shouldPermitCredentialFreePreflightRequests() throws Exception {
        mockMvc.perform(options("/api/security-probe").header(HttpHeaders.ORIGIN, "http://localhost:4200")
                                                      .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                                                      .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
                                                              "authorization"))
               .andExpect(status().isOk())
               .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"))
               .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void shouldEnforceTrailsResourceManagementRoleForApplicationMutations() throws Exception {
        mockMvc.perform(put("/api/applications").contentType("application/json")
                                                .content("{\"label\":\"Trails\"}")
                                                .with(httpBasic("tester", "password")))
               .andExpect(status().isForbidden());
        verifyNoInteractions(applicationDatabaseService);

        when(applicationDatabaseService.storeApplication(new ApplicationDefinition("Trails"))).thenReturn(
                new PersistedApplication(12L, "Trails"));
        mockMvc.perform(put("/api/applications").contentType("application/json")
                                                .content("{\"label\":\"Trails\"}")
                                                .with(httpBasic("manager", "password")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(12));

        mockMvc.perform(put("/api/applications/12/stages").contentType("application/json")
                                                          .content(
                                                                  "{\"label\":\"Production\",\"url\":\"https://trails.example\"}")
                                                          .with(httpBasic("tester", "password")))
               .andExpect(status().isForbidden());
        verifyNoInteractions(stageDatabaseService);

        StageDefinition definition = new StageDefinition("Production", "https://trails.example", List.of());
        when(stageDatabaseService.storeStage(new StageDatabaseService.StageCreation(12L, definition))).thenReturn(
                new PersistedStage(23L, 12L, "Production", "https://trails.example"));
        mockMvc.perform(put("/api/applications/12/stages").contentType("application/json")
                                                          .content(
                                                                  "{\"label\":\"Production\",\"url\":\"https://trails.example\"}")
                                                          .with(httpBasic("manager", "password")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(23));
    }

    @Test
    void shouldEnforceTrailsResourceManagementRoleForElementMutations() throws Exception {
        String request = "{\"type\":\"BUTTON\",\"label\":\"Login\",\"locatorString\":\"#login\",\"locatorType\":\"CSS\"}";
        mockMvc.perform(put("/api/applications/12/stages/23/elements").contentType("application/json")
                                                                      .content(request)
                                                                      .with(httpBasic("tester", "password")))
               .andExpect(status().isForbidden());
        verifyNoInteractions(elementDatabaseService);

        ElementDefinition definition = new ElementDefinition(ElementType.BUTTON, "Login", "#login", LocatorType.CSS);
        when(elementDatabaseService.storeElement(
                new ElementDatabaseService.ElementCreation(12L, 23L, definition))).thenReturn(
                        new PersistedElement(34L, "Login", LocatorType.CSS, 12L, 23L, "#login", ElementType.BUTTON));
        mockMvc.perform(put("/api/applications/12/stages/23/elements").contentType("application/json")
                                                                      .content(request)
                                                                      .with(httpBasic("manager", "password")))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(34));
    }

    @Test
    void shouldAllowTesterToManageTestData() throws Exception {
        TestDataSetDefinition definition = new TestDataSetDefinition("Credentials", List.of());
        when(testDataDatabaseService.storeTestData(definition)).thenReturn(
                new TestDataSet(45L, "Credentials", java.time.Instant.parse("2026-10-03T12:00:00Z"), List.of(), false));

        mockMvc.perform(put("/api/test-data").contentType("application/json")
                                             .content("{\"label\":\"Credentials\",\"values\":[]}")
                                             .with(httpBasic("tester", "password")))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(45));
    }

    @Test
    void shouldRestrictDeploymentReportsToTheDeploymentReporterRole() throws Exception {
        String request = "{\"version\":\"1.2.3\",\"deployedAt\":\"2026-10-03T12:00:00Z\"}";
        mockMvc.perform(put("/api/applications/12/stages/23/deployments").contentType("application/json")
                                                                         .content(request)
                                                                         .with(httpBasic("tester", "password")))
               .andExpect(status().isForbidden());
        verifyNoInteractions(deploymentDatabaseService);

        when(deploymentDatabaseService.storeDeployment(org.mockito.ArgumentMatchers.any())).thenReturn(
                java.util.Optional.of(new PersistedDeployment(45L, 12L, 23L, "1.2.3",
                        java.time.Instant.parse("2026-10-03T12:00:00Z"))));
        mockMvc.perform(put("/api/applications/12/stages/23/deployments").contentType("application/json")
                                                                         .content(request)
                                                                         .with(httpBasic("reporter", "password")))
               .andExpect(status().isCreated())
               .andExpect(jsonPath("$.id").value(45));
    }

}
