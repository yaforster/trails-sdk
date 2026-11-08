package io.github.yaforster.trails.adapter.api.rest.testdata;

import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestDataSetDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestDataSetDefinitionDTO;
import io.github.yaforster.trails.app.services.TestDataDatabaseService;
import io.github.yaforster.trails.core.data.TestDataSet;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.sdk.security.TrailsAuthorization;
import org.openapitools.api.TestDataApi;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

/**
 * Exposes the contract-defined test-data resource through the validated persistence composition.
 */
@RestController
public class TestDataController implements TestDataApi {

    private final TestDataDatabaseService testData;
    private final TrailsAuthorization authorization;
    private final TestDataRequestValidator requests;
    private final TestDataResponseAssembler responses;

    TestDataController(TestDataDatabaseService testData, TrailsAuthorization authorization,
            TestDataRequestValidator requests, TestDataResponseAssembler responses) {
        this.testData = Objects.requireNonNull(testData, "Test-data database service is required");
        this.authorization = Objects.requireNonNull(authorization, "Trails authorization is required");
        this.requests = Objects.requireNonNull(requests, "Test-data request validator is required");
        this.responses = Objects.requireNonNull(responses, "Test-data response assembler is required");
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
    public ResponseEntity<TestDataSetDTO> createNewTestData(TestDataSetDefinitionDTO request) {
        requests.validateDefinition(request);
        TestDataSet stored = testData.storeTestData(responses.definition(request));
        return ResponseEntity.created(responses.location(stored))
                             .body(responses.testData(stored, false, authorization.canAccessTestData()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> deleteTestData(Long testDataId) {
        requests.validateTestDataId(testDataId);
        return deletionResponse(testData.deleteTestData(testDataId));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
    public ResponseEntity<TestDataSetDTO> getTestData(Long testDataId, Boolean includeRetired) {
        requests.validateTestDataId(testDataId);
        return testData.getTestData(
                new TestDataDatabaseService.TestDataDetails(testDataId, Boolean.TRUE.equals(includeRetired)))
                       .map(dataSet -> responses.testData(dataSet, Boolean.TRUE.equals(includeRetired),
                               authorization.canAccessTestData()))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
    public ResponseEntity<PagedTestDataSetDTO> listTestData(Integer page, Integer size, Boolean includeRetired) {
        requests.validatePage(page, size);
        return ResponseEntity.ok(responses.testDataPage(
                testData.listTestData(
                        new TestDataDatabaseService.TestDataPage(page, size, Boolean.TRUE.equals(includeRetired))),
                Boolean.TRUE.equals(includeRetired), authorization.canAccessTestData()));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
    public ResponseEntity<DatabaseDeletionResultDTO> restoreTestData(Long testDataId) {
        requests.validateTestDataId(testDataId);
        return deletionResponse(testData.restoreTestData(testDataId));
    }

    @Override
    @PreAuthorize("@trailsAuthorization.canAccessTestData(authentication)")
    public ResponseEntity<TestDataSetDTO> updateTestData(Long testDataId, TestDataSetDefinitionDTO request) {
        requests.validateUpdate(testDataId, request);
        return testData.updateTestData(
                new TestDataDatabaseService.TestDataUpdate(testDataId, responses.definition(request)))
                       .map(dataSet -> responses.testData(dataSet, false, authorization.canAccessTestData()))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    private ResponseEntity<DatabaseDeletionResultDTO> deletionResponse(DatabaseDeletionResult result) {
        return ResponseEntity.status(responses.deletionStatus(result))
                             .body(responses.deletion(result));
    }

}
