package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedTestPathResult;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TestPathResultQueryService {

    Optional<PagedResult<PersistedTestPathResult>> listPathResults(TestSetPathPage testSetPathPage);

    PagedResult<PersistedTestPathResult> listPathResults(TestSetPaths testSetPaths);

    Optional<PersistedTestPathResult> getPathResult(Long pathResultId);

    record TestSetPathPage(Long testSetResultId, int page, int size) {
    }

    record TestSetPaths(Collection<Long> testSetResultIds, int page, int size) {

        public TestSetPaths {
            testSetResultIds = List.copyOf(testSetResultIds);
        }

    }

}
