package io.github.yaforster.trails.core;

import java.util.Arrays;

public record BinaryContent(byte[] bytes) {

    public BinaryContent {
        bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public byte[] bytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }

    public long size() {
        return bytes.length;
    }

}
