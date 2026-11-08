package io.github.yaforster.trails.adapter.api.rest.testrun;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestRunHistoryDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestRunResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestRunStatisticsDTO;
import io.github.yaforster.trails.app.services.TestRunResultDatabaseService;
import java.util.Objects;
import org.openapitools.api.TestrunApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes persisted test-run summaries, statistics and history through the established REST contract.
 */
@RestController
public class TestRunController implements TestrunApi {

    private final TestRunResultDatabaseService testRuns;
    private final TestRunRequestValidator requests;
    private final TestRunResponseAssembler responses;

    TestRunController(TestRunResultDatabaseService testRuns, TestRunRequestValidator requests,
            TestRunResponseAssembler responses) {
        this.testRuns = Objects.requireNonNull(testRuns, "Test run result database service is required");
        this.requests = Objects.requireNonNull(requests, "Test run request validator is required");
        this.responses = Objects.requireNonNull(responses, "Test run response assembler is required");
    }

    @Override
    public ResponseEntity<PersistedTestRunResultDTO> getTestRun(Long testRunId, Long applicationId, Long stageId) {
        requests.validateTestRun(testRunId, applicationId, stageId);
        return testRuns.getTestRunResult(
                new TestRunResultDatabaseService.TestRunResultDetails(applicationId, stageId, testRunId))
                       .map(responses::testRun)
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    public ResponseEntity<PagedTestRunDTO> listTestRuns(Long applicationId, Long stageId, Integer page, Integer size) {
        requests.validatePage(applicationId, stageId, page, size);
        return testRuns.listTestRuns(new TestRunResultDatabaseService.TestRunPage(applicationId, stageId, page, size))
                       .map(results -> responses.testRuns(applicationId, stageId, results))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    public ResponseEntity<TestRunStatisticsDTO> getTestRunStatistics(Long applicationId, Long stageId,
            Long testPlanId) {
        requests.validateTestPlan(applicationId, stageId, testPlanId);
        return testRuns.getTestRunStatistics(
                new TestRunResultDatabaseService.TestPlanStatistics(applicationId, stageId, testPlanId))
                       .map(responses::statistics)
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    public ResponseEntity<PagedTestRunHistoryDTO> getTestRunHistory(Long applicationId, Long stageId, Long testPlanId,
            Integer page, Integer size) {
        requests.validateHistory(applicationId, stageId, testPlanId, page, size);
        return testRuns.getTestRunHistory(
                new TestRunResultDatabaseService.TestPlanHistory(applicationId, stageId, testPlanId, page, size))
                       .map(responses::history)
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

}
