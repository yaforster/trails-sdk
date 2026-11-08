package io.github.yaforster.trails.core.test;

public record FileData(byte[] content, String fileName) {
    public FileData {
        content = content.clone();
    }

    @Override
    public byte[] content() {
        return content.clone();
    }
}
