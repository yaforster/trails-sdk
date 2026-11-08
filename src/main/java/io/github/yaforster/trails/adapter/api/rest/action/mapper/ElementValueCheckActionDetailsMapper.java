package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.*;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.element.ElementValueCheckAction;
import io.github.yaforster.trails.core.test.action.element.ElementValueSource;

class ElementValueCheckActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<ElementValueCheckAction, ElementValueCheckDetailsDTO> {

    ElementValueCheckActionDetailsMapper() {
        super(ElementValueCheckAction.class, ElementValueCheckDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(ElementValueCheckAction action, ActionDetailsMappingContext context) {
        ElementValueCheckDetailsDTO dto = new ElementValueCheckDetailsDTO();
        dto.setElementID(context.elementId(action.getLocatorForElementToActOn()));
        dto.setValueSource(ElementValueCheckSourceDTO.fromValue(action.getValueSource()
                                                                      .name()));
        dto.setValueName(action.getValueName());
        dto.setValueComputation(context.toDTO(action.getComputationInstruction()));
        dto.setDetailsType(ActionDetailsTypeDTO.ELEMENT_VALUE_CHECK);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, ElementValueCheckDetailsDTO details,
            ActionDetailsMappingContext context) {
        return ElementValueCheckAction.builder()
                                      .actionID(dto.getReferenceID())
                                      .label(dto.getLabel())
                                      .nextActions(dto.getNextActions())
                                      .locatorForElementToActOn(context.elementLocator(details.getElementID()))
                                      .valueSource(ElementValueSource.valueOf(details.getValueSource()
                                                                                     .getValue()))
                                      .valueName(details.getValueName())
                                      .computationInstruction(context.fromDTO(details.getValueComputation()))
                                      .position(context.position(dto))
                                      .build();
    }

}
