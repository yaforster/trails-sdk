package io.github.yaforster.trails.adapter.api.rest.testset;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestSetResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestSetResultDTO;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;
import java.util.Objects;
import org.openapitools.api.TestsetApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/** Exposes browser execution results under their owning test-run URI. */
@RestController
public class TestSetController implements TestsetApi {

    private final TestSetResultQueryService testSets;
    private final TestSetRequestValidator requests;
    private final TestSetResponseAssembler responses;

    TestSetController(TestSetResultQueryService testSets, TestSetRequestValidator requests,
            TestSetResponseAssembler responses) {
        this.testSets = Objects.requireNonNull(testSets, "Test set result query service is required");
        this.requests = Objects.requireNonNull(requests, "Test set request validator is required");
        this.responses = Objects.requireNonNull(responses, "Test set response assembler is required");
    }

    @Override
    public ResponseEntity<PagedTestSetResultDTO> listBrowserResults(Long testRunId, Long applicationId, Long stageId,
            Integer page, Integer size) {
        requests.validateBrowserResults(testRunId, applicationId, stageId, page, size);
        return testSets.listBrowserResults(
                new TestSetResultQueryService.TestRunBrowserResults(applicationId, stageId, testRunId, page, size))
                       .map(results -> responses.browserResults(applicationId, stageId, testRunId, results))
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

    @Override
    public ResponseEntity<TestSetResultDTO> getBrowserResultInTestRun(Long testRunId, Long applicationId, Long stageId,
            Long testSetResultId) {
        requests.validateBrowserResult(testRunId, applicationId, stageId, testSetResultId);
        return testSets.getTestSetResult(testSetResultId)
                       .filter(result -> result.applicationId()
                                               .equals(applicationId)
                               && result.stageId()
                                        .equals(stageId)
                               && result.testRunId()
                                        .equals(testRunId))
                       .map(responses::browserResult)
                       .map(ResponseEntity::ok)
                       .orElseGet(() -> ResponseEntity.notFound()
                                                      .build());
    }

}
