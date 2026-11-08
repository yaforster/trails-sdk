package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.SessionStorageSearchDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckSessionStorageAction;

class SessionStorageSearchActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CheckSessionStorageAction, SessionStorageSearchDetailsDTO> {

    SessionStorageSearchActionDetailsMapper() {
        super(CheckSessionStorageAction.class, SessionStorageSearchDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CheckSessionStorageAction action, ActionDetailsMappingContext context) {
        SessionStorageSearchDetailsDTO dto = new SessionStorageSearchDetailsDTO();
        dto.setStorageKey(action.getSessionStorageItemKey());
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.SESSION_STORAGE_SEARCH);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, SessionStorageSearchDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CheckSessionStorageAction.builder()
                                        .actionID(dto.getReferenceID())
                                        .label(dto.getLabel())
                                        .nextActions(dto.getNextActions())
                                        .sessionStorageItemKey(details.getStorageKey())
                                        .computationInstruction(context.fromDTO(details.getValueComputation()))
                                        .position(context.position(dto))
                                        .build();
    }

}
