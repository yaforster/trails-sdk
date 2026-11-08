package io.github.yaforster.trails.core.persisted;

public record PersistedArtifactFile(Long id, String fileName, String contentType, byte[] content) {
    public PersistedArtifactFile {
        // Older persistence providers represent unavailable screenshot content with null.
        if (content == null) {
            content = new byte[0];
        } else {
            content = content.clone();
        }
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
