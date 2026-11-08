package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDetailsTypeDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ViewportMoveDetailsDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ViewportMoveDirectionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ViewportMoveDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ViewportMoveUnitDTO;
import io.github.yaforster.trails.core.test.Action;
import io.github.yaforster.trails.core.test.action.browser.viewport.MoveViewportAction;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMove;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveDirection;
import io.github.yaforster.trails.core.test.action.browser.viewport.ViewportMoveUnit;

class MoveViewportActionDetailsMapper
        extends
            AbstractActionDetailsVariantMapper<MoveViewportAction, ViewportMoveDetailsDTO> {

    MoveViewportActionDetailsMapper() {
        super(MoveViewportAction.class, ViewportMoveDetailsDTO.class);
    }

    @Override
    protected ActionDetailsDTO toVariantDTO(MoveViewportAction action, ActionDetailsMappingContext context) {
        ViewportMoveDetailsDTO dto = new ViewportMoveDetailsDTO();
        dto.setMovement(ViewportMoveDTO.fromValue(action.getMovement()
                                                        .name()));
        dto.setDirection(ViewportMoveDirectionDTO.fromValue(action.getDirection()
                                                                  .name()));
        dto.setAmount(action.getAmount());
        dto.setUnit(ViewportMoveUnitDTO.fromValue(action.getUnit()
                                                        .name()));
        dto.setDelayAfterMoveMillis(Math.max(0L, action.getDelayAfterMoveMillis()));
        dto.setDetailsType(ActionDetailsTypeDTO.VIEWPORT_MOVE);
        return dto;
    }

    @Override
    protected Action fromVariantDTO(ActionDefinitionDTO dto, ViewportMoveDetailsDTO details,
            ActionDetailsMappingContext context) {
        return MoveViewportAction.builder()
                                 .actionID(dto.getReferenceID())
                                 .label(dto.getLabel())
                                 .nextActions(dto.getNextActions())
                                 .movement(ViewportMove.valueOf(details.getMovement()
                                                                       .getValue()))
                                 .direction(ViewportMoveDirection.valueOf(details.getDirection()
                                                                                 .getValue()))
                                 .amount(Math.max(0D, details.getAmount()))
                                 .unit(ViewportMoveUnit.valueOf(details.getUnit()
                                                                       .getValue()))
                                 .delayAfterMoveMillis(context.nonNegativeOrZero(details.getDelayAfterMoveMillis()))
                                 .position(context.position(dto))
                                 .build();
    }

}
