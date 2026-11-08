package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.TypingDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.element.TypingAction;

class TypingActionDetailsMapper extends AbstractActionDetailsVariantMapper<TypingAction, TypingDetailsDTO> {

    TypingActionDetailsMapper() {
        super(TypingAction.class, TypingDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(TypingAction action, ActionDetailsMappingContext context) {
        TypingDetailsDTO dto = new TypingDetailsDTO();
        dto.setElementID(context.elementId(action.getLocatorForElementToActOn()));
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setClearBeforeTyping(action.isClearBeforeTyping());
        dto.setDelayAfterClearMillis(action.getDelayAfterClearMillis());
        dto.setDetailsType(ActionDetailsTypeDTO.TYPING);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, TypingDetailsDTO details,
            ActionDetailsMappingContext context) {
        return TypingAction.builder()
                           .actionID(dto.getReferenceID())
                           .label(dto.getLabel())
                           .nextActions(dto.getNextActions())
                           .locatorForElementToActOn(context.elementLocator(details.getElementID()))
                           .computationInstruction(context.fromDTO(details.getValueComputation()))
                           .clearBeforeTyping(Boolean.TRUE.equals(details.getClearBeforeTyping()))
                           .delayAfterClearMillis(context.nonNegativeOrZero(details.getDelayAfterClearMillis()))
                           .position(context.position(dto))
                           .build();
    }

}
