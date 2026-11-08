package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TextCheckDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.element.TextCheckAction;

class TextCheckActionDetailsMapper extends AbstractActionDetailsVariantMapper<TextCheckAction, TextCheckDetailsDTO> {

    TextCheckActionDetailsMapper() {
        super(TextCheckAction.class, TextCheckDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(TextCheckAction action, ActionDetailsMappingContext context) {
        TextCheckDetailsDTO dto = new TextCheckDetailsDTO();
        dto.setElementID(context.elementId(action.getLocatorForElementToActOn()));
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.TEXT_CHECK);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, TextCheckDetailsDTO details,
            ActionDetailsMappingContext context) {
        return TextCheckAction.builder()
                              .actionID(dto.getReferenceID())
                              .label(dto.getLabel())
                              .nextActions(dto.getNextActions())
                              .locatorForElementToActOn(context.elementLocator(details.getElementID()))
                              .computationInstruction(context.fromDTO(details.getValueComputation()))
                              .position(context.position(dto))
                              .build();
    }

}
