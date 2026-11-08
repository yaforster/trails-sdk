package io.github.yaforster.trails.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public record GeneratedFile(String fileName, ContentWriter contentWriter) {

    public void writeTo(OutputStream outputStream) throws IOException {
        contentWriter.writeTo(outputStream);
    }

    public byte[] content() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeTo(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not materialize generated file content", exception);
        }
    }

}
