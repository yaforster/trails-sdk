package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.LocalStorageSearchDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.storage.CheckLocalStorageAction;

class LocalStorageSearchActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CheckLocalStorageAction, LocalStorageSearchDetailsDTO> {

    LocalStorageSearchActionDetailsMapper() {
        super(CheckLocalStorageAction.class, LocalStorageSearchDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CheckLocalStorageAction action, ActionDetailsMappingContext context) {
        LocalStorageSearchDetailsDTO dto = new LocalStorageSearchDetailsDTO();
        dto.setStorageKey(action.getLocalStorageItemKey());
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.LOCAL_STORAGE_SEARCH);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, LocalStorageSearchDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CheckLocalStorageAction.builder()
                                      .actionID(dto.getReferenceID())
                                      .label(dto.getLabel())
                                      .nextActions(dto.getNextActions())
                                      .localStorageItemKey(details.getStorageKey())
                                      .computationInstruction(context.fromDTO(details.getValueComputation()))
                                      .position(context.position(dto))
                                      .build();
    }

}
