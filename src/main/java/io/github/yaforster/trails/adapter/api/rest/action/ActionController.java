package io.github.yaforster.trails.adapter.api.rest.action;

import io.github.yaforster.trails.adapter.api.rest.action.mapper.ActionElementReferences;
import io.github.yaforster.trails.adapter.api.rest.model.PagedActionDTO;
import io.github.yaforster.trails.app.services.ActionQueryService;
import io.github.yaforster.trails.core.PagedResult;
import io.github.yaforster.trails.core.persisted.PersistedAction;
import java.util.Objects;
import org.openapitools.api.ActionApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ActionController implements ActionApi {

    private final ActionQueryService actionQueryService;
    private final ActionElementReferenceResolver elementReferences;
    private final ActionResponseAssembler responses;
    private final ActionRequestValidator requests;

    ActionController(ActionQueryService actionQueryService, ActionElementReferenceResolver elementReferences,
            ActionRequestValidator requests, ActionResponseAssembler responses) {
        this.actionQueryService = Objects.requireNonNull(actionQueryService, "Action query service is required");
        this.elementReferences = Objects.requireNonNull(elementReferences, "Element reference resolver is required");
        this.requests = Objects.requireNonNull(requests, "Action request validator is required");
        this.responses = Objects.requireNonNull(responses, "Action response assembler is required");
    }

    @Override
    public ResponseEntity<PagedActionDTO> getActions(Long applicationId, Long stageId, Long testPlanId, Integer page,
            Integer size) {
        requests.validatePage(applicationId, stageId, testPlanId, page, size);
        return actionQueryService.listActions(
                new ActionQueryService.ActionPage(applicationId, stageId, testPlanId, page, size))
                                 .map(pagedActions -> assemblePage(applicationId, stageId, testPlanId, pagedActions))
                                 .map(ResponseEntity::ok)
                                 .orElse(ResponseEntity.notFound()
                                                       .build());
    }

    private PagedActionDTO assemblePage(Long applicationId, Long stageId, Long testPlanId,
            PagedResult<PersistedAction> page) {
        ActionElementReferences references = elementReferences.forPage(applicationId, stageId, page.items());
        return responses.actions(applicationId, stageId, testPlanId, page, references);
    }

}
