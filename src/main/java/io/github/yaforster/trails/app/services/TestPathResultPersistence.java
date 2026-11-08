package io.github.yaforster.trails.app.services;

public interface TestPathResultPersistence {

    Long store(NamedTestPath testPath);

    record NamedTestPath(Long testSetResultId, String pathLabel) {
    }

}
