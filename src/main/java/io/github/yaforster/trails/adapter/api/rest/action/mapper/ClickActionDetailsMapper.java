package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ClickDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.element.ClickAction;

class ClickActionDetailsMapper extends AbstractActionDetailsVariantMapper<ClickAction, ClickDetailsDTO> {

    ClickActionDetailsMapper() {
        super(ClickAction.class, ClickDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(ClickAction action, ActionDetailsMappingContext context) {
        ClickDetailsDTO dto = new ClickDetailsDTO();
        dto.setElementID(context.elementId(action.getLocatorForElementToActOn()));
        dto.setDetailsType(ActionDetailsTypeDTO.CLICK);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, ClickDetailsDTO details,
            ActionDetailsMappingContext context) {
        return ClickAction.builder()
                          .actionID(dto.getReferenceID())
                          .label(dto.getLabel())
                          .nextActions(dto.getNextActions())
                          .locatorForElementToActOn(context.elementLocator(details.getElementID()))
                          .position(context.position(dto))
                          .build();
    }

}
