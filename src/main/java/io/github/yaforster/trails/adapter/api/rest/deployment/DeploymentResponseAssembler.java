package io.github.yaforster.trails.adapter.api.rest.deployment;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDTO;
import io.github.yaforster.trails.adapter.api.rest.model.DeploymentDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedDeploymentDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.definition.DeploymentDefinition;
import io.github.yaforster.trails.core.persisted.PersistedDeployment;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;

/** Maps deployment outcomes to contract DTOs and Trails-controlled navigation links. */
final class DeploymentResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    DeploymentDefinition definition(DeploymentDefinitionDTO request) {
        return new DeploymentDefinition(request.getVersion()
                                               .trim(),
                request.getDeployedAt()
                       .toInstant());
    }

    DeploymentDTO deployment(PersistedDeployment deployment) {
        Objects.requireNonNull(deployment, "Persisted deployment is required");
        return new DeploymentDTO().id(deployment.id())
                                  .applicationId(deployment.applicationId())
                                  .stageId(deployment.stageId())
                                  .version(deployment.version())
                                  .deployedAt(OffsetDateTime.ofInstant(deployment.deployedAt(), ZoneOffset.UTC))
                                  .links(Map.of("self",
                                          link(deploymentUri(deployment.applicationId(), deployment.stageId(),
                                                  deployment.id()), HttpMethod.GET),
                                          "collection",
                                          link(collectionUri(deployment.applicationId(), deployment.stageId(),
                                                  DEFAULT_PAGE, DEFAULT_PAGE_SIZE), HttpMethod.GET)));
    }

    PagedDeploymentDTO deployments(Long applicationId, Long stageId, PagedResult<PersistedDeployment> page,
            boolean canReportDeployments) {
        Objects.requireNonNull(page, "Deployment page is required");
        PagedDeploymentDTO response = new PagedDeploymentDTO().page(page.page())
                                                              .size(page.size())
                                                              .totalElements(Math.toIntExact(page.totalItems()))
                                                              .totalPages(page.totalPages())
                                                              .items(page.items()
                                                                         .stream()
                                                                         .map(this::deployment)
                                                                         .toList());
        response.setLinks(pagingLinks(applicationId, stageId, page, canReportDeployments));
        return response;
    }

    URI location(PersistedDeployment deployment) {
        Objects.requireNonNull(deployment, "Persisted deployment is required");
        return URI.create(deploymentUri(deployment.applicationId(), deployment.stageId(), deployment.id()));
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, PagedResult<PersistedDeployment> page,
            boolean canReportDeployments) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(PaginationLinks.forPage(page,
                pageNumber -> collectionUri(applicationId, stageId, pageNumber, page.size())));
        if (canReportDeployments) {
            links.put("create", link(collectionUri(applicationId, stageId), HttpMethod.PUT));
        }
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri, HttpMethod method) {
        return new LinkDTO().href(uri)
                            .method(method.name())
                            .templated(false);
    }

    private String collectionUri(Long applicationId, Long stageId, int page, int size) {
        return collectionUri(applicationId, stageId) + "?page=" + page + "&size=" + size;
    }

    private String collectionUri(Long applicationId, Long stageId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/deployments";
    }

    private String deploymentUri(Long applicationId, Long stageId, Long deploymentId) {
        return collectionUri(applicationId, stageId) + "/" + deploymentId;
    }

}
