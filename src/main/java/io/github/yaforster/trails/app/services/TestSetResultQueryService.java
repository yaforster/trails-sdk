package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestSetResult;

import java.util.Optional;

public interface TestSetResultQueryService {

    Optional<PagedResult<PersistedTestSetResult>> listBrowserResults(TestRunBrowserResults testRunBrowserResults);

    Optional<PersistedTestSetResult> getTestSetResult(Long testSetResultId);

    record TestRunBrowserResults(Long applicationId, Long stageId, Long testRunId, int page, int size) {
    }

}
