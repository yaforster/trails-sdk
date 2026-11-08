package io.github.yaforster.trails.core.data;

import io.github.yaforster.trails.core.BinaryContent;

public record Screenshot(BinaryContent content, String fileName, String contentType) {

    public long size() {
        return content.size();
    }

}
