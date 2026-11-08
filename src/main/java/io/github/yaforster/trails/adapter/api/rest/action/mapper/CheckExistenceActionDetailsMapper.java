package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CheckExistenceDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.element.CheckExistenceAction;

class CheckExistenceActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CheckExistenceAction, CheckExistenceDetailsDTO> {

    CheckExistenceActionDetailsMapper() {
        super(CheckExistenceAction.class, CheckExistenceDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CheckExistenceAction action, ActionDetailsMappingContext context) {
        CheckExistenceDetailsDTO dto = new CheckExistenceDetailsDTO();
        dto.setElementID(context.elementId(action.getLocatorForElementToActOn()));
        dto.setDetailsType(ActionDetailsTypeDTO.CHECK_EXISTENCE);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, CheckExistenceDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CheckExistenceAction.builder()
                                   .actionID(dto.getReferenceID())
                                   .label(dto.getLabel())
                                   .nextActions(dto.getNextActions())
                                   .locatorForElementToActOn(context.elementLocator(details.getElementID()))
                                   .position(context.position(dto))
                                   .build();
    }

}
