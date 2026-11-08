package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.CoordinateClickDetailsDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.CoordinateClickAction;
import io.github.yaforster.trails.core.test.action.browser.ViewportCoordinates;

class CoordinateClickActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<CoordinateClickAction, CoordinateClickDetailsDTO> {

    CoordinateClickActionDetailsMapper() {
        super(CoordinateClickAction.class, CoordinateClickDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(CoordinateClickAction action, ActionDetailsMappingContext context) {
        CoordinateClickDetailsDTO dto = new CoordinateClickDetailsDTO();
        dto.setDetailsType(ActionDetailsTypeDTO.COORDINATE_CLICK);
        dto.setxCoordinate(action.getViewportCoordinates()
                                 .xCoordinate());
        dto.setyCoordinate(action.getViewportCoordinates()
                                 .yCoordinate());
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, CoordinateClickDetailsDTO details,
            ActionDetailsMappingContext context) {
        return CoordinateClickAction.builder()
                                    .actionID(dto.getReferenceID())
                                    .label(dto.getLabel())
                                    .nextActions(dto.getNextActions())
                                    .viewportCoordinates(
                                            new ViewportCoordinates(details.getxCoordinate(), details.getyCoordinate()))
                                    .position(context.position(dto))
                                    .build();
    }

}
