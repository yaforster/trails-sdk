package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.GeneratedFile;

import java.util.Optional;

public interface PrintFacadeService {

    Optional<GeneratedFile> printPath(TestPathPrintData testPathPrintData);

    Optional<GeneratedFile> printTestSet(TestSetPrintData testSetPrintData);

    Optional<GeneratedFile> printTestRun(TestRunPrintData testRunPrintData);

    record TestPathPrintData(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId) {
    }

    record TestSetPrintData(Long applicationId, Long stageId, Long testRunId, Long testSetResultId) {
    }

    record TestRunPrintData(Long applicationId, Long stageId, Long testRunId) {
    }

}
