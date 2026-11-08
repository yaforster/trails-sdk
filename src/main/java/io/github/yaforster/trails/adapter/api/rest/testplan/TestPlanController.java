package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.ApiRequestValidationException;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionElementReferences;
import io.github.yaforster.trails.adapter.api.rest.action.ActionElementReferenceResolver;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPlansDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDefinitionDTO;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.app.services.TestPlanDatabaseService;
import io.github.yaforster.trails.core.ValidationViolation;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import io.github.yaforster.trails.core.PagedResult;
import java.util.Set;
import java.net.URI;
import java.util.List;
import java.util.Objects;
import org.openapitools.api.TestPlanApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/** Exposes the contract-defined test-plan resource through validated SDK composition. */
@RestController
public class TestPlanController implements TestPlanApi {

    private final TestPlanDatabaseService testPlans;
    private final ActionElementReferenceResolver elementReferences;
    private final TrailsAuthorization authorization;
    private final StageDatabaseService stages;
    private final TestPlanRequestValidator requests;
    private final TestPlanRequestMapper requestMapper;
    private final TestPlanResponseAssembler responses;

    TestPlanController(TestPlanDatabaseService testPlans, StageDatabaseService stages,
            ActionElementReferenceResolver elementReferences, TrailsAuthorization authorization,
            TestPlanRequestValidator requests, TestPlanRequestMapper requestMapper,
            TestPlanResponseAssembler responses) {
        this.testPlans = Objects.requireNonNull(testPlans, "Test plan database service is required");
        this.elementReferences = Objects.requireNonNull(elementReferences, "Element reference resolver is required");
        this.authorization = Objects.requireNonNull(authorization, "Trails authorization is required");
        this.stages = Objects.requireNonNull(stages, "Stage database service is required");
        this.requests = Objects.requireNonNull(requests, "Test plan request validator is required");
        this.requestMapper = Objects.requireNonNull(requestMapper, "Test plan request mapper is required");
        this.responses = Objects.requireNonNull(responses, "Test plan response assembler is required");
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<PersistedTestPlanDTO> createNewTestPlan(Long applicationId, Long stageId,
            TestPlanDefinitionDTO request) {
        requests.validateCreate(applicationId, stageId, request);
        ActionElementReferences references = elementReferences.forDefinition(request.getTestSteps());
        PersistedTestPlan testPlan = testPlans.storeTestPlan(
                requestMapper.definition(applicationId, stageId, request, references));
        return ResponseEntity.ok(responses.persistedTestPlan(applicationId, stageId, testPlan,
                testPlans.hasActions(testPlan.id()), false, permissions()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> deleteTestPlan(Long applicationId, Long stageId, Long testPlanId) {
        requests.validateTestPlanReference(applicationId, stageId, testPlanId);
        return deletionResponse(applicationId, stageId, testPlans.deleteTestPlan(
                new TestPlanDatabaseService.TestPlanReference(applicationId, stageId, testPlanId)));
    }

    @Override
    public ResponseEntity<PersistedTestPlanDTO> getTestPlan(Long applicationId, Long stageId, Long testPlanId,
            Boolean includeRetired) {
        requests.validateTestPlanReference(applicationId, stageId, testPlanId);
        return testPlans.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(applicationId,
                stageId, testPlanId, Boolean.TRUE.equals(includeRetired)))
                        .map(testPlan -> responses.persistedTestPlan(applicationId, stageId, testPlan,
                                testPlans.hasActions(testPlan.id()), Boolean.TRUE.equals(includeRetired),
                                permissions()))
                        .map(ResponseEntity::ok)
                        .orElseGet(() -> ResponseEntity.notFound()
                                                       .build());
    }

    @Override
    public ResponseEntity<PagedTestPlansDTO> listTestPlans(Long applicationId, Long stageId, Integer page, Integer size,
            Boolean includeRetired) {
        requests.validatePage(applicationId, stageId, page, size);
        boolean retirementIncluded = Boolean.TRUE.equals(includeRetired);
        TestPlanDatabaseService.TestPlanPage query = new TestPlanDatabaseService.TestPlanPage(applicationId, stageId,
                page, size, retirementIncluded);
        PagedResult<PersistedTestPlan> plansPage = testPlans.getTestPlans(query);
        List<Long> planIds = plansPage.items()
                                      .stream()
                                      .map(PersistedTestPlan::id)
                                      .toList();
        Set<Long> planIdsWithActions = testPlans.findTestPlanIdsWithActions(
                new TestPlanDatabaseService.TestPlanActions(applicationId, stageId, planIds));
        return ResponseEntity.ok(responses.testPlans(applicationId, stageId, plansPage, planIdsWithActions,
                retirementIncluded, permissions()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<PersistedTestPlanDTO> promoteTestPlan(Long applicationId, Long sourceStageId, Long testPlanId,
            Long targetStageId) {
        requests.validatePromotion(applicationId, sourceStageId, testPlanId, targetStageId);
        if (stages.getStage(new StageDatabaseService.StageDetails(applicationId, sourceStageId, false))
                  .isEmpty()
                || stages.getStage(new StageDatabaseService.StageDetails(applicationId, targetStageId, false))
                         .isEmpty()
                || testPlans.getPersistedTestPlan(new TestPlanDatabaseService.PersistedTestPlanDetails(applicationId,
                        sourceStageId, testPlanId, false))
                            .isEmpty()) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return testPlans.promoteTestPlan(
                new TestPlanDatabaseService.TestPlanPromotion(applicationId, sourceStageId, testPlanId, targetStageId))
                        .map(testPlan -> promotedTestPlan(applicationId, targetStageId, testPlan))
                        .orElseThrow(this::promotionCopyFailed);
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> restoreTestPlan(Long applicationId, Long stageId,
            Long testPlanId) {
        requests.validateTestPlanReference(applicationId, stageId, testPlanId);
        return deletionResponse(applicationId, stageId, testPlans.restoreTestPlan(
                new TestPlanDatabaseService.TestPlanReference(applicationId, stageId, testPlanId)));
    }

    private ResponseEntity<DatabaseDeletionResultDTO> deletionResponse(Long applicationId, Long stageId,
            DatabaseDeletionResult result) {
        return ResponseEntity.status(responses.deletionStatus(result))
                             .body(responses.deletion(applicationId, stageId, result));
    }

    private ResponseEntity<PersistedTestPlanDTO> promotedTestPlan(Long applicationId, Long targetStageId,
            PersistedTestPlan testPlan) {
        URI location = URI.create(
                "/api/applications/" + applicationId + "/stages/" + targetStageId + "/testPlans/" + testPlan.id());
        return ResponseEntity.created(location)
                             .body(responses.persistedTestPlan(applicationId, targetStageId, testPlan,
                                     testPlans.hasActions(testPlan.id()), false, permissions()));
    }

    private TestPlanResponseAssembler.TestPlanPermissions permissions() {
        return new TestPlanResponseAssembler.TestPlanPermissions(authorization.canManageResources(),
                authorization.canExecuteTests());
    }

    private ApiRequestValidationException promotionCopyFailed() {
        return new ApiRequestValidationException(List.of(new ValidationViolation("TEST_PLAN_PROMOTION_COPY_FAILED",
                "Test plan promotion failed because one or more actions could not be copied to the target stage. "
                        + "Make sure every referenced element has an active matching element in the target stage.",
                "/targetStageId")));
    }

}
