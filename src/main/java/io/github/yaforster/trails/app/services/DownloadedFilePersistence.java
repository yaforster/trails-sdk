package io.github.yaforster.trails.app.services;

import io.github.yaforster.trails.core.test.FileData;

public interface DownloadedFilePersistence {

    void store(DownloadedFile downloadedFile);

    record DownloadedFile(Long testPathResultId, FileData file) {
    }

}
