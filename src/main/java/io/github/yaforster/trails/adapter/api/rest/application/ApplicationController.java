package io.github.yaforster.trails.adapter.api.rest.application;

import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ApplicationDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedApplicationDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedApplicationDTO;
import io.github.yaforster.trails.app.services.ApplicationDatabaseService;
import io.github.yaforster.trails.app.services.StageDatabaseService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.definition.ApplicationDefinition;
import io.github.yaforster.trails.core.persisted.PersistedApplication;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import java.util.Objects;
import java.util.Set;
import org.openapitools.api.ApplicationApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the contract-defined application resource through the validated persistence composition.
 */
@RestController
public class ApplicationController implements ApplicationApi {

    private final ApplicationDatabaseService applications;
    private final StageDatabaseService stages;
    private final TrailsAuthorization authorization;
    private final ApplicationRequestValidator requests;
    private final ApplicationResponseAssembler responses;

    ApplicationController(ApplicationDatabaseService applications, StageDatabaseService stages,
            TrailsAuthorization authorization, ApplicationRequestValidator requests,
            ApplicationResponseAssembler responses) {
        this.applications = Objects.requireNonNull(applications, "Application database service is required");
        this.stages = Objects.requireNonNull(stages, "Stage database service is required");
        this.authorization = Objects.requireNonNull(authorization, "Trails authorization is required");
        this.requests = Objects.requireNonNull(requests, "Application request validator is required");
        this.responses = Objects.requireNonNull(responses, "Application response assembler is required");
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<PersistedApplicationDTO> createNewApplication(ApplicationDefinitionDTO request) {
        requests.validateCreate(request);
        PersistedApplication application = applications.storeApplication(new ApplicationDefinition(request.getLabel()));
        return ResponseEntity.ok(responses.persistedApplication(application));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> deleteApplication(Long applicationId) {
        requests.validateApplicationId(applicationId);
        return deletionResponse(applications.deleteApplication(applicationId));
    }

    @Override
    public ResponseEntity<ApplicationDTO> getApplication(Long applicationId, Boolean includeRetired) {
        return applications.getApplication(
                new ApplicationDatabaseService.ApplicationDetails(applicationId, Boolean.TRUE.equals(includeRetired)))
                           .map(application -> responses.application(application,
                                   stages.hasStages(new StageDatabaseService.ApplicationReference(application.id())),
                                   Boolean.TRUE.equals(includeRetired), authorization.canManageResources()))
                           .map(ResponseEntity::ok)
                           .orElseGet(() -> ResponseEntity.notFound()
                                                          .build());
    }

    @Override
    public ResponseEntity<PagedApplicationDTO> listApplications(Integer page, Integer size, Boolean includeRetired) {
        requests.validatePage(page, size);
        boolean retirementIncluded = Boolean.TRUE.equals(includeRetired);
        ApplicationDatabaseService.ApplicationPage query = new ApplicationDatabaseService.ApplicationPage(page, size,
                retirementIncluded);
        PagedResult<PersistedApplication> applicationsPage = applications.getApplications(query);
        Set<Long> applicationIdsWithStages = stages.findApplicationIdsWithStages(
                new StageDatabaseService.ApplicationsWithStages(applicationsPage.items()
                                                                                .stream()
                                                                                .map(PersistedApplication::id)
                                                                                .toList()));
        return ResponseEntity.ok(responses.applications(applicationsPage, applicationIdsWithStages, retirementIncluded,
                authorization.canManageResources()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canManageResources(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> restoreApplication(Long applicationId) {
        requests.validateApplicationId(applicationId);
        return deletionResponse(applications.restoreApplication(applicationId));
    }

    private ResponseEntity<DatabaseDeletionResultDTO> deletionResponse(DatabaseDeletionResult result) {
        return ResponseEntity.status(responses.deletionStatus(result))
                             .body(responses.deletion(result));
    }

}
