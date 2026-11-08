package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import io.github.yaforster.trails.app.services.ActionResultScreenshotQueryService;
import io.github.yaforster.trails.app.services.ArtifactQueryService;
import io.github.yaforster.trails.app.services.HierarchyValidationService;
import java.util.Objects;
import org.openapitools.api.ArtifactApi;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves artifacts only after the complete containing browser and path hierarchy is verified.
 */
@RestController
public class ArtifactController implements ArtifactApi {

    private final ArtifactQueryService artifacts;
    private final ActionResultScreenshotQueryService screenshots;
    private final HierarchyValidationService hierarchies;
    private final ArtifactRequestValidator requests;
    private final ArtifactResponseAssembler responses;

    ArtifactController(ArtifactQueryService artifacts, ActionResultScreenshotQueryService screenshots,
            HierarchyValidationService hierarchies, ArtifactRequestValidator requests,
            ArtifactResponseAssembler responses) {
        this.artifacts = Objects.requireNonNull(artifacts, "Artifact query service is required");
        this.screenshots = Objects.requireNonNull(screenshots, "Action-result screenshot query service is required");
        this.hierarchies = Objects.requireNonNull(hierarchies, "Hierarchy validation service is required");
        this.requests = Objects.requireNonNull(requests, "Artifact request validator is required");
        this.responses = Objects.requireNonNull(responses, "Artifact response assembler is required");
    }

    @Override
    public ResponseEntity<Resource> downloadArtifactInTestPath(Long applicationId, Long stageId, Long testRunId,
            Long testSetResultId, Long pathResultId, Long artifactId) {
        requests.validateArtifact(applicationId, stageId, testRunId, testSetResultId, pathResultId, artifactId);
        if (!belongsToPath(applicationId, stageId, testRunId, testSetResultId, pathResultId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return artifacts.findArtifactInPath(new ArtifactQueryService.PathArtifact(pathResultId, artifactId))
                        .map(file -> responses.download(file, false))
                        .orElseGet(() -> ResponseEntity.notFound()
                                                       .build());
    }

    @Override
    public ResponseEntity<Resource> downloadActionResultScreenshotInTestPath(Long applicationId, Long stageId,
            Long testRunId, Long testSetResultId, Long pathResultId, Long actionResultId) {
        requests.validateScreenshot(applicationId, stageId, testRunId, testSetResultId, pathResultId, actionResultId);
        if (!belongsToPath(applicationId, stageId, testRunId, testSetResultId, pathResultId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return screenshots.findInPath(
                new ActionResultScreenshotQueryService.PathActionScreenshot(pathResultId, actionResultId))
                          .map(file -> responses.download(file, true))
                          .orElseGet(() -> ResponseEntity.notFound()
                                                         .build());
    }

    @Override
    public ResponseEntity<PagedArtifactDTO> listArtifactsInTestPath(Long applicationId, Long stageId, Long testRunId,
            Long testSetResultId, Long pathResultId, Integer page, Integer size) {
        requests.validateList(applicationId, stageId, testRunId, testSetResultId, pathResultId, page, size);
        if (!belongsToPath(applicationId, stageId, testRunId, testSetResultId, pathResultId)) {
            return ResponseEntity.notFound()
                                 .build();
        }
        return ResponseEntity.ok(responses.artifacts(applicationId, stageId, testRunId, testSetResultId, pathResultId,
                artifacts.listArtifacts(new ArtifactQueryService.PathArtifactPage(pathResultId, page, size))));
    }

    private boolean belongsToPath(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId) {
        return hierarchies.pathBelongsToHierarchy(new HierarchyValidationService.TestPathHierarchy(applicationId,
                stageId, testRunId, testSetResultId, pathResultId));
    }

}
