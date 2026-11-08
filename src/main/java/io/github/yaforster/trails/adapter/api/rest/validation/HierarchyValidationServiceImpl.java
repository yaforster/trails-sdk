package io.github.yaforster.trails.adapter.api.rest.validation;

import io.github.yaforster.trails.app.services.HierarchyValidationService;
import io.github.yaforster.trails.app.services.TestPathResultQueryService;
import io.github.yaforster.trails.app.services.TestSetResultQueryService;

/**
 * Verifies that a nested result URI names objects in one persisted execution hierarchy.
 */
public final class HierarchyValidationServiceImpl implements HierarchyValidationService {

    private final TestSetResultQueryService testSetResultQueryService;
    private final TestPathResultQueryService testPathResultQueryService;

    public HierarchyValidationServiceImpl(TestSetResultQueryService testSetResultQueryService,
            TestPathResultQueryService testPathResultQueryService) {
        this.testSetResultQueryService = java.util.Objects.requireNonNull(testSetResultQueryService,
                "Test set result query service is required");
        this.testPathResultQueryService = java.util.Objects.requireNonNull(testPathResultQueryService,
                "Test path result query service is required");
    }

    @Override
    public boolean testSetBelongsToRun(TestSetHierarchy testSetHierarchy) {
        return testSetResultQueryService.getTestSetResult(testSetHierarchy.testSetResultId())
                                        .map(setResult -> setResult.applicationId()
                                                                   .equals(testSetHierarchy.applicationId())
                                                && setResult.stageId()
                                                            .equals(testSetHierarchy.stageId())
                                                && setResult.testRunId()
                                                            .equals(testSetHierarchy.testRunId()))
                                        .orElse(false);
    }

    @Override
    public boolean pathBelongsToHierarchy(TestPathHierarchy testPathHierarchy) {
        if (!testSetBelongsToRun(new TestSetHierarchy(testPathHierarchy.applicationId(), testPathHierarchy.stageId(),
                testPathHierarchy.testRunId(), testPathHierarchy.testSetResultId()))) {
            return false;
        }
        return testPathResultQueryService.getPathResult(testPathHierarchy.pathResultId())
                                         .map(pathResult -> pathResult.testSetResultId()
                                                                      .equals(testPathHierarchy.testSetResultId()))
                                         .orElse(false);
    }

}
