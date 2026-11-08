package io.github.yaforster.trails.core.persisted;

public record PersistedStage(Long id, Long applicationId, String label, String url, boolean retired) {

    public PersistedStage(Long id, Long applicationId, String label, String url) {
        this(id, applicationId, label, url, false);
    }
}
