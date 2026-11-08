package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.TestSetResult;

public interface TestSetResultPersistence {

    Long store(TestSetResultToStore testSetResult);

    record TestSetResultToStore(Long testRunId, TestSetResult testSetResult) {
    }

}
