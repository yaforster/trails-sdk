package io.github.yaforster.trails.adapter.api.rest.testplan;

import io.github.yaforster.trails.adapter.api.rest.common.PaginationLinks;
import io.github.yaforster.trails.adapter.api.rest.deletion.DeletionResponseMapper;
import io.github.yaforster.trails.adapter.api.rest.model.DatabaseDeletionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PagedTestPlansDTO;
import io.github.yaforster.trails.adapter.api.rest.model.PersistedTestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TestPlanGroupDTO;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.deletion.DatabaseDeletionResult;
import io.github.yaforster.trails.core.definition.TestPlanGroup;
import io.github.yaforster.trails.core.persisted.PersistedTestPlan;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/** Maps test-plan outcomes to contract DTOs and Trails-controlled navigation links. */
final class TestPlanResponseAssembler {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_PAGE_SIZE = 20;
    PersistedTestPlanDTO persistedTestPlan(Long applicationId, Long stageId, PersistedTestPlan testPlan,
            boolean hasActions, boolean includeRetired, TestPlanPermissions permissions) {
        Objects.requireNonNull(testPlan, "Persisted test plan is required");
        return new PersistedTestPlanDTO().id(testPlan.id())
                                         .label(testPlan.label())
                                         .retired(testPlan.retired())
                                         .groups(groups(testPlan.groups()))
                                         .links(testPlanLinks(applicationId, stageId, testPlan, hasActions,
                                                 includeRetired, permissions));
    }

    PagedTestPlansDTO testPlans(Long applicationId, Long stageId, PagedResult<PersistedTestPlan> page,
            Set<Long> testPlanIdsWithActions, boolean includeRetired, TestPlanPermissions permissions) {
        Objects.requireNonNull(page, "Test plan page is required");
        Objects.requireNonNull(testPlanIdsWithActions, "Test plan action inventory is required");
        PagedTestPlansDTO response = new PagedTestPlansDTO().page(page.page())
                                                            .size(page.size())
                                                            .totalElements(Math.toIntExact(page.totalItems()))
                                                            .totalPages(page.totalPages())
                                                            .items(page.items()
                                                                       .stream()
                                                                       .map(testPlan -> listItem(applicationId, stageId,
                                                                               testPlan,
                                                                               testPlanIdsWithActions.contains(
                                                                                       testPlan.id()),
                                                                               includeRetired, permissions))
                                                                       .toList());
        response.setLinks(pagingLinks(applicationId, stageId, page, includeRetired, permissions));
        return response;
    }

    DatabaseDeletionResultDTO deletion(Long applicationId, Long stageId, DatabaseDeletionResult result) {
        return DeletionResponseMapper.toDto(result, Map.of("collection",
                link(collectionUri(applicationId, stageId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false), HttpMethod.GET)));
    }

    HttpStatus deletionStatus(DatabaseDeletionResult result) {
        return DeletionResponseMapper.status(result);
    }

    private TestPlanDTO listItem(Long applicationId, Long stageId, PersistedTestPlan testPlan, boolean hasActions,
            boolean includeRetired, TestPlanPermissions permissions) {
        Map<String, LinkDTO> links = baseLinks(applicationId, stageId, testPlan, hasActions, includeRetired);
        if (permissions.canManageResources()) {
            if (testPlan.retired()) {
                links.put("restore",
                        link(operationUri(applicationId, stageId, testPlan.id(), "restore"), HttpMethod.POST));
            } else {
                links.put("delete", link(operationUri(applicationId, stageId, testPlan.id(), null), HttpMethod.DELETE));
            }
        }
        return new TestPlanDTO().id(testPlan.id())
                                .label(testPlan.label())
                                .links(Map.copyOf(links));
    }

    private Map<String, LinkDTO> testPlanLinks(Long applicationId, Long stageId, PersistedTestPlan testPlan,
            boolean hasActions, boolean includeRetired, TestPlanPermissions permissions) {
        Map<String, LinkDTO> links = baseLinks(applicationId, stageId, testPlan, hasActions, includeRetired);
        links.put("statistics", link(statisticsUri(applicationId, stageId, testPlan.id()), HttpMethod.GET));
        links.put("history", link(historyUri(applicationId, stageId, testPlan.id()), HttpMethod.GET));
        if (permissions.canExecuteTests()) {
            links.put("execute", link("/api/test", HttpMethod.POST));
        }
        if (permissions.canManageResources()) {
            if (testPlan.retired()) {
                links.put("restore",
                        link(operationUri(applicationId, stageId, testPlan.id(), "restore"), HttpMethod.POST));
            } else {
                links.put("delete", link(operationUri(applicationId, stageId, testPlan.id(), null), HttpMethod.DELETE));
                links.put("promote", new LinkDTO().href(promoteUri(applicationId, stageId, testPlan.id()))
                                                  .method(HttpMethod.POST.name())
                                                  .templated(true));
            }
        }
        return Map.copyOf(links);
    }

    private Map<String, LinkDTO> baseLinks(Long applicationId, Long stageId, PersistedTestPlan testPlan,
            boolean hasActions, boolean includeRetired) {
        Map<String, LinkDTO> links = new LinkedHashMap<>();
        links.put("collection",
                link(collectionUri(applicationId, stageId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, includeRetired),
                        HttpMethod.GET));
        if (hasActions) {
            links.put("steps", link(stepsUri(applicationId, stageId, testPlan.id()), HttpMethod.GET));
        }
        links.put("self", link(testPlanUri(applicationId, stageId, testPlan.id(), includeRetired), HttpMethod.GET));
        return links;
    }

    private Map<String, LinkDTO> pagingLinks(Long applicationId, Long stageId, PagedResult<PersistedTestPlan> page,
            boolean includeRetired, TestPlanPermissions permissions) {
        Map<String, LinkDTO> links = new LinkedHashMap<>(PaginationLinks.forPage(page,
                pageNumber -> collectionUri(applicationId, stageId, pageNumber, page.size(), includeRetired)));
        if (permissions.canManageResources()) {
            links.put("create", link(collectionUri(applicationId, stageId, DEFAULT_PAGE, DEFAULT_PAGE_SIZE, false),
                    HttpMethod.PUT));
        }
        return Map.copyOf(links);
    }

    private List<TestPlanGroupDTO> groups(List<TestPlanGroup> groups) {
        return Objects.requireNonNullElse(groups, List.<TestPlanGroup>of())
                      .stream()
                      .map(group -> new TestPlanGroupDTO(group.xCoordinate()
                                                              .doubleValue(),
                              group.yCoordinate()
                                   .doubleValue(),
                              group.widthPixels()
                                   .doubleValue(),
                              group.heightPixels()
                                   .doubleValue()).label(group.label())
                                                  .actionReferenceIds(Objects.requireNonNullElse(
                                                          group.actionReferenceIds(), List.of())))
                      .toList();
    }

    private LinkDTO link(String uri, HttpMethod method) {
        return new LinkDTO().href(uri)
                            .method(method.name())
                            .templated(false);
    }

    private String collectionUri(Long applicationId, Long stageId, int page, int size, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans?page=" + page + "&size=" + size
                + "&includeRetired=" + includeRetired;
    }

    private String testPlanUri(Long applicationId, Long stageId, Long testPlanId, boolean includeRetired) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId
                + "?includeRetired=" + includeRetired;
    }

    private String operationUri(Long applicationId, Long stageId, Long testPlanId, String operation) {
        String resource = "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId;
        if (operation == null) {
            return resource;
        }
        return resource + "/" + operation;
    }

    private String stepsUri(Long applicationId, Long stageId, Long testPlanId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId
                + "/actions?page=0&size=20";
    }

    private String statisticsUri(Long applicationId, Long stageId, Long testPlanId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId + "/statistics";
    }

    private String historyUri(Long applicationId, Long stageId, Long testPlanId) {
        return "/api/applications/" + applicationId + "/stages/" + stageId + "/testPlans/" + testPlanId
                + "/metrics/history?page=0&size=50";
    }

    private String promoteUri(Long applicationId, Long sourceStageId, Long testPlanId) {
        return "/api/applications/" + applicationId + "/stages/" + sourceStageId + "/testPlans/" + testPlanId
                + "/promote{?targetStageId}";
    }

    record TestPlanPermissions(boolean canManageResources, boolean canExecuteTests) {
    }

}
