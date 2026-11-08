package io.github.yaforster.trails.adapter.api.rest.action.mapper;

import io.github.yaforster.trails.adapter.api.rest.common.mapper.PositionMapper;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDTO;
import io.github.yaforster.trails.adapter.api.rest.model.ActionDefinitionDTO;
import io.github.yaforster.trails.core.test.Action;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ActionMapper {

    private final PositionMapper positionMapper;
    private final ActionDetailsMapper actionDetailsMapper;

    public ActionDTO toDTO(Action action, ActionElementReferences elements) {
        ActionDTO dto = new ActionDTO();
        dto.setActionID(action.getActionID());
        dto.nextActions(action.getNextActions());
        dto.setDetails(actionDetailsMapper.toDTO(action, elements));
        dto.setLabel(action.getLabel());
        dto.setPosition(positionMapper.toDTO(action.getPosition()));
        return dto;
    }

    public Action fromDefinitionDTO(ActionDefinitionDTO dto, ActionElementReferences elements) {
        return actionDetailsMapper.fromDTO(dto, elements);
    }

}
