package io.github.yaforster.trails.adapter.api.rest.artifact;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.ArtifactDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedArtifactDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedArtifact;
import io.github.yaforster.trails.core.persisted.PersistedArtifactFile;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Maps artifact metadata and binary content without trusting persisted media-type or filename text.
 */
final class ArtifactResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;

    PagedArtifactDTO artifacts(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId, PagedResult<PersistedArtifact> page) {
        Objects.requireNonNull(page, "Artifact page is required");
        PagedArtifactDTO response = new PagedArtifactDTO().page(page.page())
                                                          .size(page.size())
                                                          .totalElements(Math.toIntExact(page.totalItems()))
                                                          .totalPages(page.totalPages())
                                                          .items(page.items()
                                                                     .stream()
                                                                     .map(artifact -> artifact(applicationId, stageId,
                                                                             testRunId, testSetResultId, pathResultId,
                                                                             artifact))
                                                                     .toList());
        response.setLinks(pagingLinks(applicationId, stageId, testRunId, testSetResultId, pathResultId, page));
        return response;
    }

    ResponseEntity<Resource> download(PersistedArtifactFile file, boolean inline) {
        Objects.requireNonNull(file, "Artifact file is required");
        byte[] content = Objects.requireNonNull(file.content(), "Artifact file content is required");
        Resource resource = new ByteArrayResource(content);
        return ResponseEntity.ok()
                             .contentType(mediaType(file.contentType()))
                             .contentLength(content.length)
                             .header(HttpHeaders.CONTENT_DISPOSITION,
                                     contentDisposition(file.fileName(), inline).toString())
                             .body(resource);
    }

    private ArtifactDTO artifact(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId, PersistedArtifact artifact) {
        Objects.requireNonNull(artifact, "Persisted artifact is required");
        String artifactUri = artifactsUri(applicationId, stageId, testRunId, testSetResultId, pathResultId) + "/"
                + artifact.id();
        ArtifactDTO.TypeEnum type = switch (artifact.type()) {
            case SCREENSHOT -> ArtifactDTO.TypeEnum.SCREENSHOT;
            case FILE -> ArtifactDTO.TypeEnum.OTHER;
        };
        return new ArtifactDTO().id(artifact.id())
                                .type(type)
                                .filename(artifact.filename())
                                .links(Map.of("self", link(artifactUri), "collection", link(
                                        artifactsUri(applicationId, stageId, testRunId, testSetResultId, pathResultId)
                                                + "?page=0&size=20")));
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId, PagedResult<PersistedArtifact> page) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(
                PaginationLinks.forPage(page, pageNumber -> artifactsUri(applicationId, stageId, testRunId,
                        testSetResultId, pathResultId, pageNumber, page.size())));
        links.put("pathResult", link(pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId)));
        return Map.copyOf(links);
    }

    private MediaType mediaType(String rawMediaType) {
        try {
            if (rawMediaType == null || rawMediaType.isBlank()) {
                return MediaType.APPLICATION_OCTET_STREAM;
            }
            return MediaType.parseMediaType(rawMediaType);
        } catch (IllegalArgumentException ignored) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private ContentDisposition contentDisposition(String filename, boolean inline) {
        String displayFilename = filename;
        if (displayFilename == null || displayFilename.isBlank()) {
            displayFilename = "artifact";
        }
        ContentDisposition.Builder disposition;
        if (inline) {
            disposition = ContentDisposition.inline();
        } else {
            disposition = ContentDisposition.attachment();
        }
        return disposition.filename(displayFilename, StandardCharsets.UTF_8)
                          .build();
    }

    private LinkDTO link(String uri) {
        return new LinkDTO().href(uri)
                            .method(HttpMethod.GET.name())
                            .templated(false);
    }

    private String artifactsUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId) {
        return pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId) + "/artifacts";
    }

    private String artifactsUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId, int page, int size) {
        return artifactsUri(applicationId, stageId, testRunId, testSetResultId, pathResultId) + "?page=" + page
                + "&size=" + size;
    }

    private String pathUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
        return "/api/testruns/" + applicationId + "/" + stageId + "/" + testRunId + "/browsers/" + testSetResultId
                + "/paths/" + pathResultId;
    }

}
