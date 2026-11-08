package io.github.yaforster.trails.core.test;

import java.util.List;

public record TestPathResult(List<Result> results, List<FileData> downloadedFiles) {
    public TestPathResult {
        results = List.copyOf(results);
        downloadedFiles = List.copyOf(downloadedFiles);
    }

}
