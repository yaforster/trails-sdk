package io.github.yaforster.trails.adapter.api.rest.print;

import io.github.yaforster.trails.core.GeneratedFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Streams generated PDF content without materializing the complete document in application memory.
 */
final class PrintResponseAssembler {

    ResponseEntity<Resource> pdf(GeneratedFile file) {
        Objects.requireNonNull(file, "Generated print file is required");
        GeneratedFileStream input = openStream(file);
        return ResponseEntity.ok()
                             .contentType(MediaType.APPLICATION_PDF)
                             .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                                                                                        .filename(file.fileName(),
                                                                                                StandardCharsets.UTF_8)
                                                                                        .build()
                                                                                        .toString())
                             .body(new InputStreamResource(input));
    }

    private GeneratedFileStream openStream(GeneratedFile file) {
        try {
            return GeneratedFileStream.open(file);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not open PDF response stream", exception);
        }
    }

}
