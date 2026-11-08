package io.github.yaforster.trails.core.data;

import java.util.Arrays;

public final class TrailsScreenshotFile {

    private final String filename;
    private final byte[] content;

    TrailsScreenshotFile(String filename, byte[] content) {
        this.filename = filename;
        this.content = Arrays.copyOf(content, content.length);
    }

    public long size() {
        return content.length;
    }

    public String filename() {
        return filename;
    }

    public ImageType imageType() {
        return ImageTypeDetector.detect(content);
    }

    public byte[] content() {
        return Arrays.copyOf(content, content.length);
    }

}
