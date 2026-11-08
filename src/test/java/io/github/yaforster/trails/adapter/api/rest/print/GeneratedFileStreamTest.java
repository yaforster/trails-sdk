package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.core.GeneratedFile;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class GeneratedFileStreamTest {
    @Test
    void shouldReadSuccessfully_whenReaderStartsBeforeRendering() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        GeneratedFile file = new GeneratedFile("test.pdf", output -> {
            try {
                release.await();
            } catch (InterruptedException failure) {
                throw new IOException(failure);
            }
            output.write(new byte[]{1, 2, 3});
        });
        try (GeneratedFileStream stream = GeneratedFileStream.open(file);
                java.util.concurrent.ExecutorService readers = Executors.newVirtualThreadPerTaskExecutor()) {
            java.util.concurrent.Future<byte[]> content = readers.submit(stream::readAllBytes);
            release.countDown();
            assertThat(content.get(5, TimeUnit.SECONDS)).containsExactly(1, 2, 3);
        } finally {
            release.countDown();
        }
    }

    @Test
    void shouldReportRenderingFailure_insteadOfSuccessfulEndOfStream() throws Exception {
        GeneratedFile file = new GeneratedFile("test.pdf", output -> {
            output.write(1);
            throw new IOException("renderer failed");
        });
        try (GeneratedFileStream stream = GeneratedFileStream.open(file)) {
            assertThatThrownBy(stream::readAllBytes).isInstanceOf(IOException.class)
                                                    .hasMessage("Could not render PDF response");
        }
    }

    @Test
    void shouldInterruptProducer_whenClientClosesStream() throws Exception {
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        GeneratedFile file = new GeneratedFile("test.pdf", output -> {
            started.countDown();
            try {
                new CountDownLatch(1).await();
            } catch (InterruptedException failure) {
                cancelled.countDown();
            }
        });
        GeneratedFileStream stream = GeneratedFileStream.open(file);
        try {
            if (!started.await(5, TimeUnit.SECONDS))
                throw new AssertionError("Producer did not start");
        } finally {
            stream.close();
        }
        assertThat(cancelled.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
