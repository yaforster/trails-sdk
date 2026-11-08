package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.Result;

public interface ActionResultPersistence {

    void store(ActionResult actionResult);

    record ActionResult(Long testPathResultId, int executionOrder, Result result) {
    }

}
