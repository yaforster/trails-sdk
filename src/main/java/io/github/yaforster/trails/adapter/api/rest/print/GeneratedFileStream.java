package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.core.GeneratedFile;
import org.jspecify.annotations.NonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** A connected, bounded pipe whose EOF also confirms successful rendering. Closing cancels the producer. */
final class GeneratedFileStream extends FilterInputStream {
    private static final int PIPE_BUFFER_BYTES = 64 * 1024;
    private static final String RENDER_FAILED_ERROR_MESSAGE = "Could not render PDF response";
    private final CompletableFuture<Void> rendered = new CompletableFuture<>();
    private final Thread producer;

    private GeneratedFileStream(GeneratedFile file, PipedInputStream input, PipedOutputStream output) {
        super(input);
        producer = Thread.ofVirtual()
                         .unstarted(() -> render(file, output));
        producer.start();
    }

    static GeneratedFileStream open(GeneratedFile file) throws IOException {
        PipedInputStream input = new PipedInputStream(PIPE_BUFFER_BYTES);
        PipedOutputStream output = new PipedOutputStream(input);
        return new GeneratedFileStream(file, input, output);
    }

    private void render(GeneratedFile file, PipedOutputStream output) {
        try (output) {
            try {
                file.writeTo(output);
                rendered.complete(null);
            } catch (Throwable failure) {
                // Complete before closing the pipe so an unsuccessful render cannot appear as clean EOF.
                rendered.completeExceptionally(failure);
            }
        } catch (IOException failure) {
            rendered.completeExceptionally(failure);
        }
    }

    @Override
    public int read() throws IOException {
        return confirmEndOfStream(in.read());
    }

    @Override
    public int read(byte @NonNull [] buffer, int offset, int length) throws IOException {
        return confirmEndOfStream(in.read(buffer, offset, length));
    }

    private int confirmEndOfStream(int count) throws IOException {
        if (count == -1) {
            try {
                rendered.join();
            } catch (CompletionException failure) {
                throw new IOException(RENDER_FAILED_ERROR_MESSAGE, failure.getCause());
            }
        }
        return count;
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            producer.interrupt();
        }
    }
}
