package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ResizeViewportDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.ResizeViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportDimensions;

class ResizeViewportActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<ResizeViewportAction, ResizeViewportDetailsDTO> {

    ResizeViewportActionDetailsMapper() {
        super(ResizeViewportAction.class, ResizeViewportDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(ResizeViewportAction action, ActionDetailsMappingContext context) {
        ResizeViewportDetailsDTO dto = new ResizeViewportDetailsDTO();
        dto.setDetailsType(ActionDetailsTypeDTO.RESIZE_VIEWPORT);
        dto.setViewportWidth(action.getViewportDimensions()
                                   .width());
        dto.setViewportHeight(action.getViewportDimensions()
                                    .height());
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, ResizeViewportDetailsDTO details,
            ActionDetailsMappingContext context) {
        return ResizeViewportAction.builder()
                                   .actionID(dto.getReferenceID())
                                   .label(dto.getLabel())
                                   .nextActions(dto.getNextActions())
                                   .viewportDimensions(new ViewportDimensions(details.getViewportWidth(),
                                           details.getViewportHeight()))
                                   .position(context.position(dto))
                                   .build();
    }

}
