package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.model.ActionResultChainDTO;
import io.github.yaforster.trails.app.services.ActionResultQueryService;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import java.util.Objects;
import org.openapitools.api.ActionResultApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the persisted, ordered action chain only inside its verified execution hierarchy.
 */
@RestController
public class ActionResultController implements ActionResultApi {

    private final ActionResultQueryService actionResults;
    private final HierarchyValidationService hierarchies;
    private final ActionResultRequestValidator requests;
    private final ActionResultResponseAssembler responses;
    private final ActionResultScreenshotQueryService screenshots;

    ActionResultController(ActionResultQueryService actionResults, HierarchyValidationService hierarchies,
            ActionResultRequestValidator requests, ActionResultResponseAssembler responses,
            ActionResultScreenshotQueryService screenshots) {
        this.actionResults = Objects.requireNonNull(actionResults, "Action result query service is required");
        this.hierarchies = Objects.requireNonNull(hierarchies, "Hierarchy validation service is required");
        this.requests = Objects.requireNonNull(requests, "Action result request validator is required");
        this.responses = Objects.requireNonNull(responses, "Action result response assembler is required");
        this.screenshots = Objects.requireNonNull(screenshots, "Action result screenshot query service is required");
    }

    @Override
    public ResponseEntity<ActionResultChainDTO> listActionResultsChainInTestPath(Long applicationId, Long stageId,
            Long testRunId, Long testSetResultId, Long pathResultId) {
        requests.validate(applicationId, stageId, testRunId, testSetResultId, pathResultId);
        if (!belongsToPath(applicationId, stageId, testRunId, testSetResultId, pathResultId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return actionResults.listActionResultChain(pathResultId)
                            .map(results -> responses.chain(applicationId, stageId, testRunId, testSetResultId,
                                    pathResultId, results, screenshots.findActionResultIdsInPath(pathResultId)))
                            .map(ResponseEntity::ok)
                            .orElseGet(() -> ResponseEntity.notFound()
                                                           .build());
    }

    private boolean belongsToPath(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId) {
        return hierarchies.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(applicationId,
                stageId, testRunId, testSetResultId, pathResultId));
    }

}
