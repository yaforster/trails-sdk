package io.github.yaforster.trails.adapter.api.rest.actionresult;

import io.github.yaforster.trails.adapter.api.rest.model.ActionResultChainDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionResultDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LinkDTO;
import io.github.yaforster.trails.core.persisted.PersistedActionResult;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.http.HttpMethod;

/**
 * Maps the persisted, ordered action chain to its contract representation and scoped links.
 */
final class ActionResultResponseAssembler {

    ActionResultChainDTO chain(Long applicationId, Long stageId, Long testRunId, Long testSetResultId,
            Long pathResultId, List<PersistedActionResult> results, Set<Long> screenshotResultIds) {
        Objects.requireNonNull(results, "Ordered action results are required");
        String pathUri = pathUri(applicationId, stageId, testRunId, testSetResultId, pathResultId);
        String chainUri = pathUri + "/actions/chain";
        return new ActionResultChainDTO().items(results.stream()
                                                       .map(result -> actionResult(result, screenshotResultIds,
                                                               chainUri, pathUri))
                                                       .toList())
                                         .links(Map.of("self", link(chainUri), "pathResult", link(pathUri)));
    }

    private ActionResultDTO actionResult(PersistedActionResult result, Set<Long> screenshotResultIds, String chainUri,
            String pathUri) {
        Objects.requireNonNull(result, "Persisted action result is required");
        ActionResultDTO response = new ActionResultDTO().id(result.id())
                                                        .actionID(result.actionId())
                                                        .label(result.label())
                                                        .resultMessage(result.message())
                                                        .resultType(ActionResultDTO.ResultTypeEnum.valueOf(
                                                                result.resultType()
                                                                      .name()));
        Map<String, LinkDTO> links = new java.util.LinkedHashMap<>();
        links.put("collection", link(chainUri));
        links.put("pathResult", link(pathUri));
        if (screenshotResultIds.contains(result.id())) {
            links.put("screenshot", link(chainUri + "/screenshots/" + result.id()));
        }
        response.setLinks(Map.copyOf(links));
        return response;
    }

    private LinkDTO link(String uri) {
        return new LinkDTO().href(uri)
                            .method(HttpMethod.GET.name())
                            .templated(false);
    }

    private String pathUri(Long applicationId, Long stageId, Long testRunId, Long testSetResultId, Long pathResultId) {
        return "/api/testruns/" + applicationId + "/" + stageId + "/" + testRunId + "/browsers/" + testSetResultId
                + "/paths/" + pathResultId;
    }

}
