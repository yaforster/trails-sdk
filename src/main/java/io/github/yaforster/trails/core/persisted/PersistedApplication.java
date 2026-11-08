package io.github.yaforster.trails.core.persisted;

public record PersistedApplication(Long id, String label, boolean retired) {

    public PersistedApplication(Long id, String label) {
        this(id, label, false);
    }
}
