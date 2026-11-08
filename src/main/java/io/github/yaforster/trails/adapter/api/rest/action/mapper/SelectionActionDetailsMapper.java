package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.SelectionDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.element.SelectAction;

class SelectionActionDetailsMapper extends AbstractActionDetailsVariantMapper<SelectAction, SelectionDetailsDTO> {

    SelectionActionDetailsMapper() {
        super(SelectAction.class, SelectionDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(SelectAction action, ActionDetailsMappingContext context) {
        SelectionDetailsDTO dto = new SelectionDetailsDTO();
        dto.setElementID(context.elementId(action.getLocatorForElementToActOn()));
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.SELECTION);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, SelectionDetailsDTO details,
            ActionDetailsMappingContext context) {
        return SelectAction.builder()
                           .actionID(dto.getReferenceID())
                           .label(dto.getLabel())
                           .nextActions(dto.getNextActions())
                           .locatorForElementToActOn(context.elementLocator(details.getElementID()))
                           .computationInstruction(context.fromDTO(details.getValueComputation()))
                           .position(context.position(dto))
                           .build();
    }

}
