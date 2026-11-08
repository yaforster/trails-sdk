package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionMapper;
import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionElementReferences;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.springframework.http.HttpMethod;

/** Maps Action query outcomes to contract DTOs and Trails-controlled navigation links. */
final class ActionResponseAssembler {

    private static final int FIRST_PAGE = 0;
    private final ActionMapper actions;

    ActionResponseAssembler(ActionMapper actions) {
        this.actions = Objects.requireNonNull(actions, "Action mapper is required");
    }

    PagedActionDTO actions(Long applicationId, Long stageId, Long testPlanId, PagedResult<PersistedAction> page,
            ActionElementReferences elements) {
        Objects.requireNonNull(page, "Action page is required");
        PagedActionDTO response = new PagedActionDTO().page(page.page())
                                                      .size(page.size())
                                                      .totalElements(Math.toIntExact(page.totalItems()))
                                                      .totalPages(page.totalPages())
                                                      .items(page.items()
                                                                 .stream()
                                                                 .map(action -> action(action, elements))
                                                                 .toList());
        response.setLinks(pagingLinks(applicationId, stageId, testPlanId, page));
        return response;
    }

    private ActionDTO action(PersistedAction action, ActionElementReferences elements) {
        ActionDTO response = actions.toDTO(action.action(), elements);
        response.setReferenceID(action.referenceId());
        return response;
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, Long testPlanId,
            PagedResult<PersistedAction> page) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(PaginationLinks.forPage(page,
                pageNumber -> collectionUri(applicationId, stageId, testPlanId, pageNumber, page.size())));
        links.put("testPlan", link(testPlanUri(applicationId, stageId, testPlanId)));
        return Map.copyOf(links);
    }

    private LinkDTO link(String uri) {
        return new LinkDTO().href(uri)
                            .method(HttpMethod.GET.name())
                            .templated(false);
    }

    private String collectionUri(Long applicationId, Long stageId, Long testPlanId, int page, int size) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId
                + "/actions?page=" + page + "&size=" + size;
    }

    private String testPlanUri(Long applicationId, Long stageId, Long testPlanId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId
                + "?includeRetired=false";
    }

}
