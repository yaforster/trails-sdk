package io.github.yaforster.trails.app.services;

public interface HierarchyValidationService {

    boolean testSetBelongsToRun(TestSetHierarchy testSetHierarchy);

    boolean pathBelongsToHierarchy(TestPathHierarchy testPathHierarchy);

    record TestSetHierarchy(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
    }

    record TestPathHierarchy(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId) {
    }

}
