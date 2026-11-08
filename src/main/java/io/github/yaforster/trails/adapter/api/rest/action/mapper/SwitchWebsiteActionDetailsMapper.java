package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.SwitchWebsiteDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.SwitchWebsiteAction;

class SwitchWebsiteActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<SwitchWebsiteAction, SwitchWebsiteDetailsDTO> {

    SwitchWebsiteActionDetailsMapper() {
        super(SwitchWebsiteAction.class, SwitchWebsiteDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(SwitchWebsiteAction action, ActionDetailsMappingContext context) {
        SwitchWebsiteDetailsDTO dto = new SwitchWebsiteDetailsDTO();
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.SWITCH_WEBSITE);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, SwitchWebsiteDetailsDTO details,
            ActionDetailsMappingContext context) {
        return SwitchWebsiteAction.builder()
                                  .actionID(dto.getReferenceID())
                                  .label(dto.getLabel())
                                  .nextActions(dto.getNextActions())
                                  .computationInstruction(context.fromDTO(details.getValueComputation()))
                                  .position(context.position(dto))
                                  .build();
    }

}
