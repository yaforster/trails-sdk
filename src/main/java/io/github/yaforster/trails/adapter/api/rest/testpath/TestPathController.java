package io.github.yaforster.trails.adapter.api.rest.testpath;

import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPathResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPathResultDTO;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import java.util.Objects;
import org.openapitools.api.TestpathApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes test-path results only after their complete parent execution hierarchy has been verified.
 */
@RestController
public class TestPathController implements TestpathApi {

    private final TestPathResultQueryService paths;
    private final HierarchyValidationService hierarchies;
    private final TestPathRequestValidator requests;
    private final TestPathResponseAssembler responses;

    TestPathController(TestPathResultQueryService paths, HierarchyValidationService hierarchies,
            TestPathRequestValidator requests, TestPathResponseAssembler responses) {
        this.paths = Objects.requireNonNull(paths, "Test path result query service is required");
        this.hierarchies = Objects.requireNonNull(hierarchies, "Hierarchy validation service is required");
        this.requests = Objects.requireNonNull(requests, "Test path request validator is required");
        this.responses = Objects.requireNonNull(responses, "Test path response assembler is required");
    }

    @Override
    public ResponseEntity<PagedTestPathResultDTO> listPathResultsInTestSet(Long applicationId, Long stageId,
            Long testRunId, Long testSetResultId, Integer page, Integer size) {
        requests.validatePathResults(applicationId, stageId, testRunId, testSetResultId, page, size);
        if (!belongsToTestSet(applicationId, stageId, testRunId, testSetResultId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return paths.listPathResults(new TestPathResultQueryService.TestSetPathPage(testSetResultId, page, size))
                    .map(results -> responses.pathResults(applicationId, stageId, testRunId, testSetResultId, results))
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound()
                                                   .build());
    }

    @Override
    public ResponseEntity<TestPathResultDTO> getPathResultInTestSet(Long applicationId, Long stageId, Long testRunId,
            Long testSetResultId, Long pathResultId) {
        requests.validatePathResult(applicationId, stageId, testRunId, testSetResultId, pathResultId);
        if (!belongsToTestSet(applicationId, stageId, testRunId, testSetResultId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return paths.getPathResult(pathResultId)
                    .filter(path -> path.testSetResultId()
                                        .equals(testSetResultId))
                    .map(path -> responses.pathResult(applicationId, stageId, testRunId, path))
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.notFound()
                                                   .build());
    }

    private boolean belongsToTestSet(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
        return hierarchies.testSetBelongsToRun(
                new HierarchyValidationService.TestSetHierarchy(applicationId, stageId, testRunId, testSetResultId));
    }

}
